package net.mizukilab.pit.classloaders;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedTimeProfilerClassLoader extends ClassLoader{
    Map<String,Class<?>> classes = new ConcurrentHashMap();
    public CachedTimeProfilerClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> aClass1 = classes.get(name);
        if(aClass1 != null){
            return aClass1;
        }
        long time = System.currentTimeMillis();
        Class<?> aClass = super.loadClass(name);
        if(System.currentTimeMillis() - time > 100){
            System.out.println("Load class takes so long: " + aClass);
        }
        classes.put(name, aClass);
        return aClass;
    }

    @Nullable
    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }
}
