package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 异步单写线程 CSV 写入器
 * 支持 UTF-8 BOM、文件大小轮转、定时 Flush 与防阻塞队列
 */
public class PersistentPeakCsvWriter {
    private static final Logger LOGGER = Logger.getLogger(PersistentPeakCsvWriter.class.getName());

    public static final String CSV_HEADER = "time,elapsed_seconds,UHF-1_dBm,UHF-2_dBm,AE-1_dBm,AE-2_dBm,HFCT-1_dBm,HFCT-2_dBm,Spare-1_dBm,Spare-2_dBm,event\r\n";

    private final PersistentPeakSession session;
    private final PersistentPeakConfig config;
    private final BlockingQueue<PersistentPeakSnapshot> queue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong droppedSnapshots = new AtomicLong(0);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);

    private Thread writerThread;
    private PrintWriter printWriter;
    private File currentFile;
    private int fileRotationIndex = 1;
    private long currentFileBytes = 0;
    private long lastFlushTime = 0;

    public PersistentPeakCsvWriter(PersistentPeakSession session) {
        this.session = session;
        this.config = session.getConfig();
        this.queue = new ArrayBlockingQueue<>(this.config.getCsvQueueCapacity());
    }

    public synchronized void start() {
        if (running.get()) {
            return;
        }
        running.set(true);
        this.currentFile = session.getCsvFile();
        initFileWriter(this.currentFile);

        writerThread = new Thread(this::runWriterLoop, "PersistentPeak-CSV-Writer-" + session.getSessionId());
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public boolean offerSnapshot(PersistentPeakSnapshot snapshot) {
        if (!running.get() || snapshot == null) {
            return false;
        }
        // 非阻塞入队
        boolean offered = queue.offer(snapshot);
        if (!offered) {
            droppedSnapshots.incrementAndGet();
            LOGGER.warning("PersistentPeak CSV queue full! Dropped snapshot seq: " + snapshot.getSequence());
        }
        return offered;
    }

    private void runWriterLoop() {
        lastFlushTime = System.currentTimeMillis();
        while (running.get() || !queue.isEmpty()) {
            try {
                PersistentPeakSnapshot snapshot = queue.poll(200, TimeUnit.MILLISECONDS);
                if (snapshot != null) {
                    writeSnapshot(snapshot);
                }

                long now = System.currentTimeMillis();
                if (now - lastFlushTime >= config.getCsvFlushIntervalMs()) {
                    flushWriter();
                    lastFlushTime = now;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error writing persistent peak CSV", e);
            }
        }
        flushWriter();
    }

    private void writeSnapshot(PersistentPeakSnapshot snapshot) {
        // 检查是否有至少一个有效通道
        boolean hasAnyValid = false;
        for (int i = 0; i < 8; i++) {
            if (snapshot.isValid(i) && !Double.isNaN(snapshot.getPublishedDbm(i))) {
                hasAnyValid = true;
                break;
            }
        }
        // 如果尚无任何通道初始化就绪，不写入数值伪数据行
        if (!hasAnyValid) {
            return;
        }

        checkRotation();

        if (printWriter == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(new Date(snapshot.getTimestampMillis()))).append(",");
        sb.append(String.format(Locale.ROOT, "%.1f", snapshot.getElapsedSeconds())).append(",");

        for (int i = 0; i < 8; i++) {
            if (snapshot.isValid(i)) {
                sb.append(PeakValueFormatter.formatForCsv(snapshot.getPublishedDbm(i)));
            }
            sb.append(",");
        }

        String ev = snapshot.getEvent();
        long drops = droppedSnapshots.getAndSet(0);
        if (drops > 0) {
            String dropMsg = "CSV_QUEUE_DROPPED:" + drops;
            ev = (ev == null || ev.isEmpty()) ? dropMsg : ev + ";" + dropMsg;
        }

        if (ev != null && !ev.isEmpty()) {
            if (ev.contains(",") || ev.contains("\"") || ev.contains("\n")) {
                sb.append("\"").append(ev.replace("\"", "\"\"")).append("\"");
            } else {
                sb.append(ev);
            }
        }
        sb.append("\r\n");

        String row = sb.toString();
        printWriter.print(row);
        currentFileBytes += row.getBytes(StandardCharsets.UTF_8).length;
    }

    private void checkRotation() {
        long maxBytes = (long) config.getCsvMaxFileMb() * 1024L * 1024L;
        if (currentFileBytes >= maxBytes) {
            flushWriter();
            closeCurrentWriter();
            fileRotationIndex++;
            String nextFileName = String.format(Locale.ROOT, "peaks_%03d.csv", fileRotationIndex);
            currentFile = new File(session.getOutputDirectory(), nextFileName);
            initFileWriter(currentFile);
        }
    }

    private void initFileWriter(File file) {
        try {
            boolean isNew = !file.exists() || file.length() == 0;
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            printWriter = new PrintWriter(osw);

            if (isNew) {
                // 写入 UTF-8 BOM (\uFEFF)
                fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
                // 写入表头
                printWriter.print(CSV_HEADER);
                printWriter.flush();
                currentFileBytes = 3 + CSV_HEADER.getBytes(StandardCharsets.UTF_8).length;
            } else {
                currentFileBytes = file.length();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize CSV writer for file: " + file.getAbsolutePath(), e);
            printWriter = null;
        }
    }

    private void flushWriter() {
        if (printWriter != null) {
            try {
                printWriter.flush();
            } catch (Exception ignored) {
            }
        }
    }

    private void closeCurrentWriter() {
        if (printWriter != null) {
            try {
                printWriter.flush();
                printWriter.close();
            } catch (Exception ignored) {
            }
            printWriter = null;
        }
    }

    public synchronized void close() {
        if (!running.get()) {
            return;
        }
        running.set(false);
        if (writerThread != null) {
            try {
                writerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        closeCurrentWriter();
    }

    public long getDroppedSnapshots() {
        return droppedSnapshots.get();
    }

    public File getCurrentFile() {
        return currentFile;
    }
}
