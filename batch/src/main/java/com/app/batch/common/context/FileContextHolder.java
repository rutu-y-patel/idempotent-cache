package com.app.batch.common.context;

public class FileContextHolder {

    private static final ThreadLocal<String> currentFileName = new ThreadLocal<>();
    public static void set(String fileName) { currentFileName.set(fileName); }
    public static String get() { return currentFileName.get(); }
    public static void clear() { currentFileName.remove(); }
}
