package com.utils;

import io.netty.util.AttributeKey;

public class Conf {

    public final static AttributeKey<String> conf_key = AttributeKey.newInstance("conf");

    private String name; // ss + 透传
    private String mode; // ss + 透传
    private Integer localPort; // ss + 透传
    private String des; // ss + 透传
    private Boolean enable; // ss + 透传

    private String passWord; // ss
    private String encrypt; // ss
    private String serverHost; //  透传
    private Integer serverPort;//  透传

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "Conf{" +
                "name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", localPort=" + localPort +
                ", des='" + des + '\'' +
                ", passWord='" + passWord + '\'' +
                ", encrypt='" + encrypt + '\'' +
                ", serverHost='" + serverHost + '\'' +
                ", serverPort=" + serverPort +
                '}';
    }
}
