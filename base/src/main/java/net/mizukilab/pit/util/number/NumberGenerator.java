package net.mizukilab.pit.util.number;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberGenerator {

    static NumberGenerator INSTANCE;

    static {
        INSTANCE = new NumberGenerator();
    }

    public static NumberGenerator get() {
        return INSTANCE;
    }

    private final List<Integer> numsReversed;
    private Map<String, String> NUMBERS;
    private static final Pattern IS_DOT_REGEX = Pattern.compile("\\.(\\d+?)0*$");

    private static final Pattern FINISHER_MULT_DIV_PATTERN = Pattern.compile("([*/])\\(([^()+*/\\-]+)\\)");

    private static final Pattern FINISHER_ADD_SUB_PATTERN = Pattern.compile("([+-])\\(([^()]+)\\)([+\\-)])");

    private static final Pattern FINISHER_ADD_SUB_END_PATTERN = Pattern.compile("([+-])\\(([^()]+)\\)$");
    private static final Pattern FINISHER_OUTERMOST_BRACKET_PATTERN = Pattern.compile("^\\(([^()]+)\\)$");
    private static final Pattern NUM_PATTERN = Pattern.compile("\\d+|⑨");

    public static void main(String[] args) {
        NumberGenerator numberGenerator = NumberGenerator.get();
        
        for (int i = 0;i < 10000;i++) {
            numberGenerator.homo(i);
        }
    }
    public NumberGenerator() {
        this.NUMBERS = new HashMap<>();

        initializeNums();
        // Extract integer keys, filter positive, sort ascendingly
        this.numsReversed = new ArrayList<>(){
            {
                for (String key : NUMBERS.keySet()) {
                    if (isIntegerKey(key) && Integer.parseInt(key) > 0) {
                        add(Integer.parseInt(key));
                    }
                }
            }};
        Collections.sort(numsReversed);
    }

    /**
     * Initializes the Nums map with the provided key-value pairs.
     */
    private void initializeNums() {
        // Initialize with provided key-value pairs
        NUMBERS = new Object2ObjectOpenHashMap<>() {{
            put("229028", "⌈sin(114514°)+cos(114514°)⌉");  // 通过三角函数取整
            put("114514", "√(114514²)");                   // 平方根恒等式
            put("58596", "114×⌊tan(514°)⌋");               // 三角函数取整运算
            put("49654", "⌈√(114514)⌉×44");                // 平方根取整
            put("400", "√(160000)");                       // 直接平方根
            put("100", "√(10000)");                        // 完美平方
            put("25", "5×√(25)");                          // 自映射平方根
            put("16", "4^√4");                             // 根号指数
            put("9", "√81");                               // 完美平方
            put("4", "√4+√4");                             // 根号叠加
            put("1", "sin²(√1)+cos²(√1)");                 // 三角恒等式
            put("0", "sin(√1)-sin(√1)");                   // 三角零值
            put("2", "√(5-1)");                            // 代数平方根
            put("3", "√(5+4)");                            // 简单平方根
            put("5", "⌈√24⌉");                             // 向上取整
            put("⑨", "⌊√85⌋");                             // 向下取整
        }};
    }

    /**
     * Converts the given number into a string representation based on the Nums mapping.
     *
     * @param num The number to convert.
     * @return The converted string.
     */
    public String homo(double num) {
        return finisher(demolish(num));
    }

    /**
     * Finds the smallest divisor from numsReversed that is less than or equal to num.
     *
     * @param num The number to find the divisor for.
     * @return The smallest divisor.
     */
    private Integer getMinDiv(double num) {
        for (int i = numsReversed.size() - 1; i >= 0; i--) {
            int candidate = numsReversed.get(i);
            if (num >= candidate) {
                return candidate;
            }
        }
        return null; // Or throw an exception if no divisor is found
    }

    /**
     * Recursively breaks down the number into its components based on the Nums mapping.
     *
     * @param num The number to demolish.
     * @return The expression string.
     */
    private String demolish(double num) {
        if (Double.isInfinite(num) || Double.isNaN(num)) {
            return String.format("这么恶臭的%s有必要论证吗", num);
        }

        if (num < 0) {
            String positivePart = demolish(-num);
            // Replace "*1" if present
            return String.format("(⑨)*(%s)", positivePart).replace("*1", "");
        }

        if (!isInteger(num)) {
            // Handle decimal fractions
            BigDecimal bd = BigDecimal.valueOf(num).setScale(16, RoundingMode.DOWN);
            Matcher matcher = IS_DOT_REGEX.matcher(bd.toPlainString());
            if (matcher.find()) {
                int n = matcher.group(1).length();
                double scaledNum = num * Math.pow(10, n);
                return String.format("(%s)/(10)^(%d)", demolish(scaledNum), n);
            }
            // Fallback if regex doesn't match
            return "";
        }

        int intNum = (int) num;
        String key = String.valueOf(intNum);
        if (NUMBERS.containsKey(key)) {
            return key;
        }

        Integer div = getMinDiv(num);
        if (div == null || div == 0) {
            // Cannot find a suitable divisor, return the number as string
            return key;
        }

        double quotient = Math.floor(num / div);
        double remainder = num % div;

        StringBuilder sb = new StringBuilder();

        if (quotient > 0) {
            sb.append(String.format("%d*(%s)", div, demolish(quotient)));
        }

        if (remainder > 0) {
            if (!sb.isEmpty()) {
                sb.append("+");
            }
            sb.append(String.format("(%s)", demolish(remainder)));
        }

        // Replace "*1" and "+0" with empty strings
        return sb.toString().replace("*1", "").replace("+0", "");
    }

    /**
     * Processes the expression string by performing regex-based replacements.
     *
     * @param expr The expression to finish.
     * @return The finished expression.
     */
    private String finisher(String expr) {
        // Replace numbers and '⑨' with their corresponding strings in Nums
        StringBuilder replacedExpr = new StringBuilder();
        Matcher matcher = NUM_PATTERN.matcher(expr);
        int lastIndex = 0;
        while (matcher.find()) {
            replacedExpr.append(expr, lastIndex, matcher.start());
            String match = matcher.group();
            if (match.equals("⑨")) {
                replacedExpr.append(NUMBERS.getOrDefault("⑨", "⑨"));
            } else {
                String replacement = NUMBERS.getOrDefault(match, match);
                replacedExpr.append(replacement);
            }
            lastIndex = matcher.end();
        }
        replacedExpr.append(expr.substring(lastIndex));
        expr = replacedExpr.toString().replace("^", "**");

        // Perform [*|/] followed by (expr) -> replace with operator + expr
        Matcher mulDivMatcher = FINISHER_MULT_DIV_PATTERN.matcher(expr);
        while (mulDivMatcher.find()) {
            String operator = mulDivMatcher.group(1);
            String innerExpr = mulDivMatcher.group(2);
            expr = expr.replace(mulDivMatcher.group(), operator + innerExpr);
            mulDivMatcher = FINISHER_MULT_DIV_PATTERN.matcher(expr);
        }

        // Perform [+|-] followed by (expr) followed by [+|-|)] -> replace with operator + expr + trailing
        Matcher addSubMatcher = FINISHER_ADD_SUB_PATTERN.matcher(expr);
        while (addSubMatcher.find()) {
            String operator = addSubMatcher.group(1);
            String innerExpr = addSubMatcher.group(2);
            String trailing = addSubMatcher.group(3);
            expr = expr.replace(addSubMatcher.group(), operator + innerExpr + trailing);
            addSubMatcher = FINISHER_ADD_SUB_PATTERN.matcher(expr);
        }

        // Perform [+|-] followed by (expr) at the end -> replace with operator + expr
        Matcher addSubEndMatcher = FINISHER_ADD_SUB_END_PATTERN.matcher(expr);
        while (addSubEndMatcher.find()) {
            String operator = addSubEndMatcher.group(1);
            String innerExpr = addSubEndMatcher.group(2);
            expr = expr.replace(addSubEndMatcher.group(), operator + innerExpr);
            addSubEndMatcher = FINISHER_ADD_SUB_END_PATTERN.matcher(expr);
        }

        // Remove outermost brackets if present
        Matcher outermostMatcher = FINISHER_OUTERMOST_BRACKET_PATTERN.matcher(expr);
        if (outermostMatcher.matches()) {
            expr = outermostMatcher.group(1);
        }

        // Replace "+-" with "-"
        expr = expr.replace("+-", "-");

        return expr;
    }

    /**
     * Checks if a double value is an integer.
     *
     * @param num The number to check.
     * @return True if the number is an integer, false otherwise.
     */
    private boolean isInteger(double num) {
        return num == Math.floor(num) && !Double.isInfinite(num);
    }

    /**
     * Checks if a key is an integer.
     *
     * @param key The key to check.
     * @return True if the key represents an integer, false otherwise.
     */
    private boolean isIntegerKey(String key) {
        try {
            Integer.parseInt(key);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}
