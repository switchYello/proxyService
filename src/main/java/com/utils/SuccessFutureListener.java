package com.utils;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiaoming
 * 只有监听器成功才执行的监听器
 */
public abstract class SuccessFutureListener<V> implements FutureListener<V> {

    private static Logger log = LoggerFactory.getLogger(SuccessFutureListener.class);

    @Override
    public void operationComplete(Future<V> future) throws Exception {
        if (future.isSuccess()) {
            operationComplete0(future.getNow());
        } else {
            log.debug("", future.cause());
        }
    }

    public abstract void operationComplete0(V future) throws Exception;

}
