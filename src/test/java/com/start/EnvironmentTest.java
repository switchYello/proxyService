package com.start;

import com.utils.Conf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class EnvironmentTest {

    @Test
    public void getConfs() {
        List<Conf> confs = Environment.getConfs();
        for (Conf value : confs) {
            assertNotNull(value.getName());
            assertNotNull(value.getMode());
            assertNotNull(value.getDes());
            assertNotNull(value.getLocalPort());
            if ("ss".equals(value.getMode())) {
                assertNotNull(value.getPassWord());
                assertNotNull(value.getEncrypt());
            }
            if ("forward".equals(value.getMode())) {
                assertNotNull(value.getServerHost());
                assertNotNull(value.getServerPort());
            }
        }

    }
}