package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 趋势图 HTML 生成器
 * 将自包含的模板复制到会话输出目录，并在打开时自动加载/内嵌 CSV 记录，供用户在浏览器中离线查看趋势曲线
 */
public final class PeakTrendHtmlGenerator {
    private static final Logger LOGGER = Logger.getLogger(PeakTrendHtmlGenerator.class.getName());
    private static final String TEMPLATE_RESOURCE = "/peak-trend-template.html";

    private PeakTrendHtmlGenerator() {}

    public static boolean generateHtml(File targetHtmlFile, PersistentPeakConfig config) {
        return updateHtmlWithCsv(targetHtmlFile, null, config);
    }

    public static boolean updateHtmlWithCsv(File targetHtmlFile, File csvFile, PersistentPeakConfig config) {
        if (targetHtmlFile == null) {
            return false;
        }

        try {
            String template = loadTemplateString();
            if (template == null) {
                LOGGER.warning("HTML template resource " + TEMPLATE_RESOURCE + " not found in classpath, generating fallback HTML");
                return generateFallbackHtml(targetHtmlFile);
            }

            String csvContent = "";
            if (csvFile != null && csvFile.exists() && csvFile.isFile()) {
                try {
                    byte[] bytes = Files.readAllBytes(csvFile.toPath());
                    csvContent = new String(bytes, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Failed to read CSV file for embedding", e);
                }
            }

            String jsCsv = "window.__INLINE_CSV__ = " + escapeJsString(csvContent) + ";";
            String injectionTag = "/* __INLINE_CSV_INJECTION__ */";
            if (template.contains(injectionTag)) {
                template = template.replace(injectionTag, jsCsv);
            } else {
                template = template.replace("<script>", "<script>\n    " + jsCsv + "\n");
            }

            try (OutputStream out = new FileOutputStream(targetHtmlFile)) {
                out.write(template.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to generate HTML trend file: " + targetHtmlFile.getAbsolutePath(), e);
            return false;
        }
    }

    private static String loadTemplateString() {
        try (InputStream in = PeakTrendHtmlGenerator.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load template string", e);
            return null;
        }
    }

    public static String escapeJsString(String s) {
        if (s == null || s.isEmpty()) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private static boolean generateFallbackHtml(File targetHtmlFile) {
        String basicHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>持久峰值平滑趋势图</title></head>"
                + "<body style='background:#1e1e1e;color:#fff;font-family:sans-serif;padding:20px;'>"
                + "<h2>GraphApp8Pro 持久峰值趋势查看器</h2>"
                + "<p>请在同目录下使用 Excel 或数据分析工具打开 <b>peaks.csv</b> 查看平滑峰值数据。</p>"
                + "</body></html>";
        try (OutputStream out = new FileOutputStream(targetHtmlFile)) {
            out.write(basicHtml.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to write fallback HTML", e);
            return false;
        }
    }
}
