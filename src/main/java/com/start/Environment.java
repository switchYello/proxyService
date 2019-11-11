package com.start;

import com.utils.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * 所有配置
 */
public class Environment {

    private static final Logger log = LoggerFactory.getLogger(Environment.class);

    private Integer startPort;
    private String remoteSalt;
    //http代理的账号密码
    private String userName;
    private String passWord;

    public Environment(Properties properties) {
        loadData(properties);
        check();
    }

    public Environment(String fileName) {
        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile(fileName)) {
            Objects.requireNonNull(resourceAsStream, "未发现配置文件:" + fileName);
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            loadData(properties);
            check();
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }

    private void loadData(Properties properties) {
        remoteSalt = properties.getProperty("remoteSalt");
        userName = properties.getProperty("userName");
        passWord = properties.getProperty("passWord");
        String startPort = properties.getProperty("startPort");
        if (startPort != null) {
            this.startPort = Integer.valueOf(startPort);
        }
    }

    private void check() {
        Objects.requireNonNull(startPort, "未知startPort");
        Objects.requireNonNull(remoteSalt, "未知remoteSalt");
    }


    public Integer getStartPort() {
        return startPort;
    }

    public Environment setStartPort(Integer startPort) {
        this.startPort = startPort;
        return this;
    }

    public String getRemoteSalt() {
        return remoteSalt;
    }

    public Environment setRemoteSalt(String remoteSalt) {
        this.remoteSalt = remoteSalt;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public Environment setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassWord() {
        return passWord;
    }

    public Environment setPassWord(String passWord) {
        this.passWord = passWord;
        return this;
    }
}
