package net.mizukilab.pit.util.chat;

import java.text.DecimalFormat;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/3 22:44
 */
public class StringUtil {

    private static final long million = 100000000;
    private static final long tenkilo = 10000;
    public static DecimalFormat decimalFormat = new java.text.DecimalFormat("0.00");

    public static String getFormatLong(long input) {
        boolean negative = input < 0;
        input = Math.abs(input);

        if (input > million) {
            return (negative ? "-" : "") + decimalFormat.format((double) input / (double) million) + " 亿";
        } else if (input > tenkilo) {
            return (negative ? "-" : "") + decimalFormat.format((double) input / (double) tenkilo) + " 万";
        } else {
            return (negative ? "-" : "") + input + "";
        }

    }

    /**
     * 隐藏字符串中间的字符，根据长度动态调整首尾保留字符数量
     * @param input 要隐藏的字符串
     * @return 处理后的字符串
     */
    public static String hideMiddle(String input) {
        if (input == null || input.length() <= 2) {
            return input; // 如果字符串长度小于等于2，不处理
        }

        int length = input.length();
        int visibleChars; // 首尾保留字符数量

        if (length > 32) {
            visibleChars = 5; // 长度大于32，保留5位
        } else if (length > 16) {
            visibleChars = 3; // 长度大于16且小于等于32，保留3位
        } else {
            visibleChars = 2; // 默认保留2位
        }

        int hiddenChars = length - 2 * visibleChars;
        if (hiddenChars <= 0) {
            return input; // 如果中间部分无字符需要隐藏，直接返回原字符串
        }

        StringBuilder hidden = new StringBuilder();
        for (int i = 0; i < hiddenChars; i++) {
            hidden.append("*"); // 用 '*' 替换中间部分
        }

        return input.substring(0, visibleChars) + hidden + input.substring(length - visibleChars);
    }
}
