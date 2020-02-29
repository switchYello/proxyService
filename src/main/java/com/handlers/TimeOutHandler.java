package com.handlers;

import io.netty.handler.timeout.IdleStateHandler;

/**
 * hcy 2020/2/29
 */
public class TimeOutHandler extends IdleStateHandler {

    public TimeOutHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }


}
