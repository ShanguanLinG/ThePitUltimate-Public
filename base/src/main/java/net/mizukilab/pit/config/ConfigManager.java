package net.mizukilab.pit.config;

import cn.charlotte.pit.ThePit;
import lombok.Getter;
import net.mizukilab.pit.util.configuration.Configuration;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ConfigManager {
    PitGlobalConfig global;
    @Getter
    Map<Integer, PitWorldConfig> pitConfigs = new ConcurrentHashMap<>();
    int maxId;
    ThePit instance;
    @Getter
    long cursor;

    public ConfigManager(ThePit thePit) {
        this.instance = thePit;
    }

    public PitGlobalConfig getGlobal() {
        PitGlobalConfig pitGlobal = new PitGlobalConfig(instance);
        pitGlobal.load();
        return global = pitGlobal;
    }

    public int setCursor(int index) {
        this.cursor = index;
        synchronizeGlobal();
        return index;
    }

    public void nextConfig() {
        cursor++;
        synchronizeGlobal();
    }

    public void synchronize(Consumer<PitGlobalConfig> global) {
        global.accept(this.global);
    }

    public void synchronizeGlobal() {
        this.global.setCurrentMapId(cursor);
    }

    public void synchronizeLegacy() {
        PitWorldConfig selectedWorldConfig = getSelectedWorldConfig();
        Validate.notNull(selectedWorldConfig);
        this.instance.setPitConfig(selectedWorldConfig);
        this.instance.setGlobalConfig(global);
    }

    public PitWorldConfig getSelectedWorldConfig() {
        if (!pitConfigs.isEmpty()) {
            PitWorldConfig pitWorldConfig1;
            pitWorldConfig1 = getPitWorldConfig();
            if (pitWorldConfig1 == null) return null;
            synchronizeGlobal();
            return pitWorldConfig1;
        }
        try {
            AtomicBoolean atomicBoolean = new AtomicBoolean();
            Stream<Path> walk = Files.walk(instance.getDataFolder().toPath(), FileVisitOption.FOLLOW_LINKS);
            Optional<Path> worlds = walk.filter(i -> {
                File file = i.toFile();
                return file.isDirectory() && file.getName().equals("worlds");
            }).findFirst();
            walk.close();
            worlds.ifPresentOrElse(i -> {
                atomicBoolean.set(true);
                File file = i.toFile();
                if (file.exists()) {
                    File[] files = file.listFiles(s -> s.isFile() && s.exists() && s.getName().endsWith(".yml"));
                    if (files != null) {
                        for (File file1 : files) {
                            PitWorldConfig pitWorldConfig1 = new PitWorldConfig(global, instance, file1.getName(), i.toFile().getName());
                            pitWorldConfig1.load();
                            int id = pitWorldConfig1.getId();
                            if (id <= 0) {
                                System.out.println("Can't load the config which was identified as negative number or zero " + file1.getName());
                                continue;
                            }
                            if (id > this.maxId) {
                                this.maxId = id;
                            }
                            this.pitConfigs.put(id, pitWorldConfig1);
                        }
                    }
                } else {
                    file.mkdirs();
                }
            }, () -> {
                copyWorld();
                System.out.println("Didn't have any worlds, shutting down");
                Bukkit.shutdown();
            });
            boolean b = atomicBoolean.get();
            if (b) {
                if (pitConfigs.isEmpty()) {
                    b = false;
                    System.out.println("Didn't have any worlds, shutting down");
                    return null;
                } else {
                    return pitConfigs.values().iterator().next();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public void copyWorld() {
        Path targetPath = ThePit.getInstance().getDataFolder().toPath().resolve("worlds/world.yml");

        try (InputStream inputStream = getClass().getResourceAsStream("/worlds/world.yml")) {
            if (inputStream == null) {
                System.out.println("Default file cannot be found.");
                return;
            }

            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Default world has been generated.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private PitWorldConfig getPitWorldConfig() {
        PitWorldConfig pitWorldConfig1;
        while (true) {
            long l = cursor % maxId + 1;
            pitWorldConfig1 = pitConfigs.get((int) l);
            if (pitWorldConfig1 != null) {
                break;
            }
            if (maxId == 0) {
                return null;
            }
            cursor++;
        }
        return pitWorldConfig1;
    }

    @Nullable
    public PitWorldConfig getPitWorldConfigSpecific(long cursor) {
        PitWorldConfig pitWorldConfig1;
        while (true) {
            long l = cursor % maxId + 1;
            pitWorldConfig1 = pitConfigs.get((int) l);
            if (pitWorldConfig1 != null) {
                break;
            }
            if (maxId == 0) {
                return null;
            }
            cursor++;
        }
        return pitWorldConfig1;
    }

    public void save() {
        global.save();
        this.pitConfigs.values().forEach(Configuration::save);
    }

    public void reload() {
        global.load();
        instance.setGlobalConfig(global);
        pitConfigs.clear();
        cursor = 0;
        PitWorldConfig selectedWorldConfig = this.getSelectedWorldConfig();
        if (selectedWorldConfig != null) {
            instance.setPitConfig(selectedWorldConfig);
        } else {
            System.out.println("Can't find a suitable config for world map");
        }
    }
}
