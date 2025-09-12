package net.mizukilab.pit.util;

import com.google.common.collect.ImmutableSet;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    public static Collection<Class<?>> getClassesInPackage(JavaPlugin plugin, String packageName) {
        Collection<Class<?>> classes = new ArrayList();
        CodeSource codeSource = plugin.getClass().getProtectionDomain().getCodeSource();
        URL resource = codeSource.getLocation();
        String relPath = packageName.replace('.', '/');
        String resPath = resource.getPath().replace("%20", " ");
        String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");

        JarFile jarFile;
        try {
            jarFile = new JarFile(jarPath);
        } catch (IOException var16) {
            IOException e = var16;
            throw new IllegalStateException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
        }

        Enumeration<JarEntry> entries = jarFile.entries();

        while (true) {
            String className;
            do {
                if (!entries.hasMoreElements()) {
                    try {
                        jarFile.close();
                    } catch (IOException var15) {
                        IOException e = var15;
                        e.printStackTrace();
                    }

                    return ImmutableSet.copyOf(classes);
                }

                JarEntry entry = (JarEntry) entries.nextElement();
                String entryName = entry.getName();
                className = null;
                if (entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length() > relPath.length() + "/".length()) {
                    className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                }
            } while (className == null);

            Class clazz;
            try {
                clazz = plugin.getClass().getClassLoader().loadClass(className);
            } catch (NoClassDefFoundError | ClassNotFoundException var17) {
                continue;
            }

            classes.add(clazz);
        }
    }

}
