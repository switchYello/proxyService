package com.start;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utils.Conf;
import com.utils.ResourceManager;
import com.utils.Symbols;
import io.netty.channel.Channel;
import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

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
@Slf4j
public class Environment {

    private final static ObjectMapper mapper;
    private static List<Conf> confs;
    // 全局日志级别
    public static LogLevel level = LogLevel.INFO;
    public static AdvancedByteBufFormat format = AdvancedByteBufFormat.SIMPLE;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile(Symbols.CONF_NAME)) {
            Objects.requireNonNull(resourceAsStream, "未发现配置文件:" + Symbols.CONF_NAME);
            BufferedReader read = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = read.readLine()) != null) {
                sb.append(temp);
            }
            confs = mapper.readValue(sb.toString(), new TypeReference<List<Conf>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }

    static {
        if (log.isDebugEnabled()) {
            level = LogLevel.DEBUG;
            format = AdvancedByteBufFormat.HEX_DUMP;
        }
    }

    public static List<Conf> loadConfs() {
        return confs;
    }

    public static Conf getConfFromChannel(Channel channel) {
        return channel.attr(Conf.CONF_KEY).get();
    }

}
