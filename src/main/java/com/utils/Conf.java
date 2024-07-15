package com.utils;

import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Conf {

    public final static AttributeKey<Conf> CONF_KEY = AttributeKey.newInstance("conf");

    private String name; // ss + 透传
    private String mode; // ss + 透传
    private Integer localPort; // ss + 透传
    private Boolean enable; // ss + 透传

    private String serverHost; //  透传
    private Integer serverPort;//  透传
    private String passWord; // ss
    private String encrypt; // ss

}