package com.utils;

import reactor.util.annotation.Nullable;

/**
 * @author huangchaoyu
 * @since 2024/7/16 11:33
 */
public class Assert {

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }


}
