package com.utils;

import java.io.InputStream;

public class ResourceManager {

    public static InputStream gerResourceForFile(String fileName) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
    }

}
