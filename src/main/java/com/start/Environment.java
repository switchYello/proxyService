package com.start;

import com.utils.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * 所有配置
 */
public class Environment {

    private static Integer startPort;
    //代理的账号密码
    private static String userName;
    private static String passWord;

    static {
        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile("param.properties")) {
            Objects.requireNonNull(resourceAsStream, "未发现配置文件: param.properties");
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            loadData(properties);
            check();
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }

    private static void loadData(Properties properties) {
        String startPortProperties = properties.getProperty("startPort");
        if (startPortProperties != null) {
            startPort = Integer.valueOf(startPortProperties);
        }
        userName = properties.getProperty("userName");
        passWord = properties.getProperty("passWord");
    }


    private static void check() {
        Objects.requireNonNull(startPort, "未知startPort");
        Objects.requireNonNull(userName, "未知userName");
        Objects.requireNonNull(passWord, "未知passWord");
    }

    public static Integer getStartPort() {
        return startPort;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassWord() {
        return passWord;
    }
}
