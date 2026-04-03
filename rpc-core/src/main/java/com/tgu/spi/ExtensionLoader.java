package com.tgu.spi;

import com.tgu.serializers.Serializer;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoader<T extends NamedExtension> {

    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private final Map<String, T> nameMap = new ConcurrentHashMap<>();

    private final Map<Integer, T> codeMap = new ConcurrentHashMap<>();

    private final Class<T> type;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
        loadExtensions();
    }

    public static <T extends NamedExtension> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type cannot be null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface: " + type.getName());
        }
        ExtensionLoader<?> loader = LOADERS.get(type);
        if (loader != null) {
            return (ExtensionLoader<T>) loader;
        }

        ExtensionLoader<T> newLoader = new ExtensionLoader<>(type);
        ExtensionLoader<?> oldLoader = LOADERS.putIfAbsent(type, newLoader);
        return oldLoader == null ? newLoader : (ExtensionLoader<T>) oldLoader;
    }

    public T getExtension(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return nameMap.get(name.toLowerCase());
    }

    public T getExtension(int code) {
        return codeMap.get(code);
    }

    private void loadExtensions() {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(type);
        for (T extension : serviceLoader) {
            registerByName(extension);
            registerByCode(extension);
        }
    }

    private void registerByName(T extension) {
        String name = extension.getName();
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Extension name cannot be empty: " + extension.getClass().getName());
        }
        String key = name.toLowerCase();
        T old = nameMap.putIfAbsent(key, extension);
        if (old != null && old.getClass() != extension.getClass()) {
            throw new IllegalStateException("Duplicate extension name: " + name + ", type: " + type.getName());
        }
    }

    private void registerByCode(T extension) {
        if (!(extension instanceof Serializer serializer)) {
            return;
        }
        T old = codeMap.putIfAbsent(serializer.getType(), extension);
        if (old != null && old.getClass() != extension.getClass()) {
            throw new IllegalStateException("Duplicate serializer code: " + serializer.getType());
        }
    }
}
