package com.utils;

import java.io.InputStream;
import java.net.URL;

/*
 * 读取资源文件
 * */
public class ResourceManager {

    public static InputStream gerResourceForFile(String fileName) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
    }

}
