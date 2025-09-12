package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Araykal
 * @since 2025/4/7
 */
public class BannerUtil {
    public static void printFileContent(String resourcePath) {
        try (InputStream inputStream = BannerUtil.class.getResourceAsStream("/" + resourcePath)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    ThePit.getInstance().sendLogs("§c" + line);
                }
                ThePit.getInstance().sendLogs("Starting ThePitUltimate...");
                ThePit.getInstance().sendLogs("§aVersion: §e" + ThePit.getInstance().getDescription().getVersion());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
