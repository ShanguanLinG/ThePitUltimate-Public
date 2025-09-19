package net.mizukilab.pit.classloaders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedTimeProfilerClassLoader extends ClassLoader {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final Map<String, Object> loadingLocks = new ConcurrentHashMap<>();

    public CachedTimeProfilerClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> cachedClass = classes.get(name);
        if (cachedClass != null) {
            return cachedClass;
        }

        Object lock = loadingLocks.computeIfAbsent(name, k -> new Object());
        synchronized (lock) {
            cachedClass = classes.get(name);
            if (cachedClass != null) {
                return cachedClass;
            }

            long startTime = System.currentTimeMillis();
            Class<?> loadedClass = super.loadClass(name);
            long loadTime = System.currentTimeMillis() - startTime;

            if (loadTime > 1000) {
                System.out.println("Load class takes so long: " + loadedClass + " (" + loadTime + "ms)");
            }

            classes.put(name, loadedClass);
            return loadedClass;
        }
    }
}