package com.start;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utils.Conf;
import com.utils.ResourceManager;
import io.netty.channel.Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 所有配置
 */
public class Environment {

    private static final String CONF_NAME = "conf.json";
    private static List<Conf> confs;

    static {
        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile(CONF_NAME)) {
            Objects.requireNonNull(resourceAsStream, "未发现配置文件:" + CONF_NAME);
            BufferedReader read = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = read.readLine()) != null) {
                sb.append(temp);
            }
            confs = new ObjectMapper().readValue(sb.toString(), new TypeReference<List<Conf>>() {
            });

        } catch (IOException e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }


    public static List<Conf> getConfs() {
        return confs;
    }

    public static Conf getConfFromChannel(Channel channel) {
        return channel.attr(Conf.conf_key).get();
    }


}
