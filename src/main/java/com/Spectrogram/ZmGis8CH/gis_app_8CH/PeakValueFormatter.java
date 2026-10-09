package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * 峰值格式化与舍入工具类
 * 支持根据配置项动态决定小数位数（0位即无小数点的整数格式）
 */
public final class PeakValueFormatter {

    private PeakValueFormatter() {}

    /**
     * 对内部 double 数值根据全局配置的小数位数执行 HALF_UP 舍入
     *
     * @param value 原始 double
     * @return 舍入后的 double，若为 NaN 或 Infinite 则返回 Double.NaN
     */
    public static double roundForPublish(double value) {
        return roundForPublish(value, AppGlobalConfig.getDisplayDecimals());
    }

    /**
     * 对内部 double 数值指定小数位数执行 HALF_UP 舍入
     *
     * @param value 原始 double
     * @param decimals 小数位数（0-6）
     * @return 舍入后的 double，若为 NaN 或 Infinite 则返回 Double.NaN
     */
    public static double roundForPublish(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return Double.NaN;
        }
        int scale = Math.max(0, Math.min(6, decimals));
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 格式化 CSV 输出：根据配置小数位数格式化，0 位小数无小数点，无效值返回空字符串
     *
     * @param value 已经舍入后的数值
     * @return CSV 字符串（如 "-34" 或 "-34.2"），无效值返回 ""
     */
    public static String formatForCsv(double value) {
        return formatForCsv(value, AppGlobalConfig.getDisplayDecimals());
    }

    /**
     * 格式化 CSV 输出：指定小数位数，0 位小数无小数点，无效值返回空字符串
     *
     * @param value 已经舍入后的数值
     * @param decimals 小数位数（0-6）
     * @return CSV 字符串（如 "-34" 或 "-34.2"），无效值返回 ""
     */
    public static String formatForCsv(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "";
        }
        int scale = Math.max(0, Math.min(6, decimals));
        if (scale == 0) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        return String.format(Locale.ROOT, "%." + scale + "f", value);
    }

    /**
     * 格式化 UI 显示：根据配置小数位数格式化，0 位小数无小数点，无效值返回 "--"
     *
     * @param value 已经舍入后的数值
     * @return UI 显示字符串（如 "-34" 或 "-34.2" 或 "--"）
     */
    public static String formatForUi(double value) {
        return formatForUi(value, AppGlobalConfig.getDisplayDecimals());
    }

    /**
     * 格式化 UI 显示：指定小数位数，0 位小数无小数点，无效值返回 "--"
     *
     * @param value 已经舍入后的数值
     * @param decimals 小数位数（0-6）
     * @return UI 显示字符串（如 "-34" 或 "-34.2" 或 "--"）
     */
    public static String formatForUi(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "--";
        }
        int scale = Math.max(0, Math.min(6, decimals));
        if (scale == 0) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        return String.format(Locale.ROOT, "%." + scale + "f", value);
    }

    /**
     * 格式化 UI 带单位显示：如 "-34 dBm" 或 "-34.2 dBm" 或 "--"
     *
     * @param value 已经舍入后的数值
     * @param unit 单位字符串（如 "dBm"）
     * @return UI 字符串
     */
    public static String formatForUiWithUnit(double value, String unit) {
        return formatForUiWithUnit(value, unit, AppGlobalConfig.getDisplayDecimals());
    }

    /**
     * 格式化 UI 带单位显示：指定小数位数
     *
     * @param value 已经舍入后的数值
     * @param unit 单位字符串（如 "dBm"）
     * @param decimals 小数位数（0-6）
     * @return UI 字符串
     */
    public static String formatForUiWithUnit(double value, String unit, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "--";
        }
        String u = (unit != null && !unit.isEmpty()) ? " " + unit : "";
        int scale = Math.max(0, Math.min(6, decimals));
        if (scale == 0) {
            return String.format(Locale.ROOT, "%.0f", value) + u;
        }
        return String.format(Locale.ROOT, "%." + scale + "f", value) + u;
    }
}

