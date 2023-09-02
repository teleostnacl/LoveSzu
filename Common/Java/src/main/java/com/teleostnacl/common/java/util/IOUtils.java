package com.teleostnacl.common.java.util;

import java.io.Closeable;
import java.util.Collection;

public class IOUtils {
    /**
     * 关闭流
     *
     * @param closeables 需要关闭的流
     */
    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            close(closeable);
        }
    }

    /**
     * 关闭流
     *
     * @param closeables 需要关闭的流
     */
    public static void close(Collection<Closeable> closeables) {
        for (Closeable closeable : closeables) {
            close(closeable);
        }
    }

    /**
     * 关闭流
     *
     * @param closeable 需要关闭的流
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
