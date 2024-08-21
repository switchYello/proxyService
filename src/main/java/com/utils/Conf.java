package com.utils;

import io.netty.util.AttributeKey;
import lombok.Data;

@Data
public class Conf {

    public final static AttributeKey<Conf> CONF_KEY = AttributeKey.newInstance("conf");

    private String name;       // ss + 透传
    private Boolean enable;    // ss + 透传
    private String mode;       // ss + 透传
    private Integer localPort; // ss + 透传

    private String serverHost; //  透传
    private Integer serverPort;//  透传
    private String userName;   // http代理
    private String passWord;   // ss +http代理
    private String encrypt;    // ss

}