package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/*
 * 读取资源文件
 * */
public class ResourceManager {

    public static InputStream gerResourceForFile(String fileName) throws FileNotFoundException {

        InputStream in;
        ClassLoader classLoader = ResourceManager.class.getClassLoader();
        ClassLoader loader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        //如果文件存在则优先使用fileInputStream
        if (new File(fileName).exists()) {
            in = new FileInputStream(fileName);
        } else {
            in = loader.getResourceAsStream(fileName);
        }
        return in;
    }

}
