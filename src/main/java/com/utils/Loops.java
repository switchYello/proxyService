package com.utils;

import reactor.netty.resources.LoopResources;

/**
 * @author huangchaoyu
 * @since 2024/7/15 19:30
 */
public class Loops {

    public static final LoopResources ssLoopResources = LoopResources.create("SS-SERVER", 1, 1, true);

    public static final LoopResources forwardLoopResources = LoopResources.create("FORWARD-SERVER", 1, 1, true);


}
