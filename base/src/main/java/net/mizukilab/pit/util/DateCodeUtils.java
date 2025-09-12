package net.mizukilab.pit.util;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DateCodeUtils {
    private static final char[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final Map<String, LocalDate> codeToDateMap = new HashMap<>();

    public static String dateToCode(LocalDate date) {
        int year = date.getYear() % 26;
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        char firstLetter = LETTERS[year];
        char secondLetter = LETTERS[(month - 1) % 26];
        int number = (day - 1) % 10;

        String code = "" + firstLetter + secondLetter + number;

        codeToDateMap.put(code, date);

        return code;
    }

    public static LocalDate codeToDate(String code) {
        if (codeToDateMap.containsKey(code)) {
            return codeToDateMap.get(code);
        } else {
            throw new IllegalArgumentException("?");
        }
    }

}
