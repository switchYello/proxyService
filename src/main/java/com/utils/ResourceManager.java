package com.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/*
 * 读取资源文件
 * */
public class ResourceManager {

    public static InputStream gerResourceForFile(String fileName) throws FileNotFoundException {

        InputStream in = null;
        ClassLoader classLoader = ResourceManager.class.getClassLoader();
        ClassLoader loader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;

        if (loader != null) {
            in = loader.getResourceAsStream(fileName);
        }
        if (in == null) {
            in = new FileInputStream(fileName);
        }
        return in;
    }

}
