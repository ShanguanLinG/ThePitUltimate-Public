package net.mizukilab.pit.license;

import org.bukkit.plugin.java.JavaPlugin;

public class CommonLoader {
    protected static Class<?> loader = null;
    
    public static void bootstrap(JavaPlugin plugin) {
        try {
            if (loader != null) {
                loader.getMethod("start").invoke(null);
            }
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to bootstrap loader: " + e.getMessage());
        }
    }

    public static void preBootstrap(JavaPlugin plugin) {
        try {
            loader = Class.forName("net.mizukilab.pit.Loader");
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to load main loader: " + e.getMessage());
            loader = null;
        }
    }
}
