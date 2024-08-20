package com.start;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utils.Conf;
import com.utils.ResourceManager;
import com.utils.Symbols;
import io.netty.channel.Channel;
import io.netty.handler.logging.LogLevel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 所有配置
 */
@Slf4j
public class Environment {
    private static List<Conf> confs;
    // 全局日志级别
    public static LogLevel level = LogLevel.INFO;
    public static AdvancedByteBufFormat format = AdvancedByteBufFormat.SIMPLE;

    @Data
    public static class ConfigWrap {
        private List<Conf> services;
    }

    //启动时加载配置文件
    static {
        Constructor c = new Constructor(new LoaderOptions());
        c.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) {
                setSkipMissingProperties(true);
                return super.getProperty(type, name);
            }
        });
        Yaml yaml = new Yaml(c);
        try (InputStream resourceAsStream = ResourceManager.gerResourceForFile(Symbols.CONF_NAME)) {
            ConfigWrap load = yaml.loadAs(resourceAsStream, ConfigWrap.class);
            Environment.confs = load.services;
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
