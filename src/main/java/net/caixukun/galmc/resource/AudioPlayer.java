package net.caixukun.galmc.resource;

import com.mojang.blaze3d.audio.OggAudioStream;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPlayer {

    private static final ExecutorService PLAYBACK_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "AudioPlayback");
        t.setDaemon(true); // 随游戏退出自动结束
        return t;
    });

    private SourceDataLine line;
    private AudioInputStream audioInputStream;
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private volatile float volume = 1.0f; // 0.0 ~ 1.0

    /**
     * 播放本地文件
     */
    public void playID(String id) throws Exception {
        File file = GalResourceManger.get_sound(id);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            playOgg(in);
        }
    }

    /**
     * 播放 OGG 格式的音频流
     */
    public void playOgg(InputStream oggStream) throws Exception {
        // 停止当前播放
        stop();

        // 解码 OGG，得到 PCM 数据和格式
        OggAudioStream oggAudioStream = new OggAudioStream(oggStream);
        AudioFormat format = oggAudioStream.getFormat();  // javax.sound.sampled.AudioFormat
        ByteBuffer pcmData = oggAudioStream.readAll();    // 一次性读取全部 PCM 数据
        oggAudioStream.close();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Line not supported: " + format);
        }

        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        applyVolume();
        line.start();

        stopped.set(false);
        paused.set(false);

        // 在后台线程中写入 PCM 数据
        PLAYBACK_EXECUTOR.submit(() -> {
            try {
                while (pcmData.hasRemaining() && !stopped.get()) {
                    // 处理暂停
                    while (paused.get() && !stopped.get()) {
                        line.stop();
                        Thread.sleep(50);
                    }
                    if (stopped.get()) break;
                    line.start();

                    // 分块写入，避免一次性写入过多
                    int chunkSize = Math.min(pcmData.remaining(), 8192);
                    byte[] chunk = new byte[chunkSize];
                    pcmData.get(chunk);
                    line.write(chunk, 0, chunkSize);
                }
                if (!stopped.get()) {
                    line.drain();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        });
    }

    /**
     * 播放输入流（调用者负责关闭）
     */
    public void play(InputStream input) throws Exception {
        play(AudioSystem.getAudioInputStream(input));
    }

    /**
     * 实际播放逻辑：解码 AudioInputStream -> PCM -> SourceDataLine
     */
    private void play(AudioInputStream rawStream) throws Exception {
        // 先停止正在播放的音频
        stop();

        // 将源格式转换为 PCM_SIGNED
        AudioFormat baseFormat = rawStream.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
        );

        // 如果原始流不是目标 PCM 格式，则进行转换
        AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, rawStream);
        this.audioInputStream = decodedStream;

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Line not supported: " + decodedFormat);
        }

        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(decodedFormat);
        applyVolume(); // 设置初始音量
        line.start();

        stopped.set(false);
        paused.set(false);

        // 在后台线程中写入音频数据
        PLAYBACK_EXECUTOR.submit(() -> {
            try {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while (!stopped.get() && (bytesRead = decodedStream.read(buffer)) != -1) {
                    // 如果暂停，则等待
                    while (paused.get() && !stopped.get()) {
                        line.stop();
                        Thread.sleep(50);
                    }
                    if (stopped.get()) break;
                    line.start();
                    line.write(buffer, 0, bytesRead);
                }
                // 播放完毕，排空缓冲区
                if (!stopped.get()) {
                    line.drain();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        });
    }

    /**
     * 暂停播放
     */
    public void pause() {
        paused.set(true);
    }

    /**
     * 恢复播放
     */
    public void resume() {
        paused.set(false);
    }

    /**
     * 停止播放并释放资源
     */
    public void stop() {
        stopped.set(true);
        paused.set(false);
        if (line != null) {
            line.stop();
            line.flush();
        }
        // 注意：真正的关闭在 cleanup 中，由播放线程调用，也可以在此处调用
        cleanup();
    }

    /**
     * 设置音量（0.0 ~ 1.0）
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        applyVolume();
    }

    public float getVolume() {
        return volume;
    }

    public boolean isPlaying() {
        return line != null && line.isActive() && !stopped.get();
    }

    private void applyVolume() {
        if (line != null && line.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                if (gainControl != null) {
                    // MASTER_GAIN 单位是分贝，0 dB 表示最大，负值表示衰减
                    float db = (float) (20.0 * Math.log10(volume == 0.0 ? 0.0001 : volume));
                    gainControl.setValue(db);
                }
            } catch (IllegalArgumentException e) {
                // 该行不支持音量控制
            }
        }
    }

    private void cleanup() {
        if (line != null) {
            line.close();
            line = null;
        }
        if (audioInputStream != null) {
            try {
                audioInputStream.close();
            } catch (IOException ignored) {}
            audioInputStream = null;
        }
        stopped.set(true);
        paused.set(false);
    }
}
