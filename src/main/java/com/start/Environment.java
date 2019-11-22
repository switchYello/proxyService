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
import java.util.*;

/**
 * 所有配置
 */
public class Environment {

    private static final String CONF_NAME = "conf.json";
    private static Map<String, Conf> confMap = new HashMap<>();

    static {
        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile(CONF_NAME)) {
            Objects.requireNonNull(resourceAsStream, "未发现配置文件:" + CONF_NAME);
            BufferedReader read = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = read.readLine()) != null) {
                sb.append(temp);
            }
            List<Conf> confs = new ObjectMapper().readValue(sb.toString(), new TypeReference<List<Conf>>() {
            });
            for (Conf conf : confs) {
                confMap.put(conf.getName(), conf);
            }

        } catch (IOException e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }


    public static List<Conf> getConfs() {
        return new ArrayList<>(confMap.values());
    }

    public static Conf getByName(String name) {
        return confMap.get(name);
    }

    public static Conf gotConfFromChannel(Channel channel) {
        String key = channel.attr(Conf.conf_key).get();
        return getByName(key);
    }

}
