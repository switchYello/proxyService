package com.utils;

import com.start.Context;
import io.netty.handler.codec.http.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证http代理的帐号密码
 * 提供两种方式
 */
public class PasswordChecker {

    private static Logger log = LoggerFactory.getLogger(PasswordChecker.class);

    private static String proxyHead = "Proxy-Authorization";
    private static String hostHead = "Host";

    private static String realUserName = Context.getEnvironment().getUserName();
    private static String realPassword = Context.getEnvironment().getPassWord();

    //digest方式登录
    public static boolean digestLogin(HttpRequest req) {
        String s = req.headers().get(proxyHead);
        String host = req.headers().get(hostHead);
        if (s == null) {
            return false;
        }
        try {
            // Digest username="1", realm="Text", nonce="1543832167934", uri="/", response="401aaf0fe5388b02bd2a410b6f87ecd8", opaque="password", qop=auth, nc=00000001, cnonce="7bb142d0282b2405"
            log.debug("域名:{},认证头:{}", host, s);
            //移除‘Digest ’
            s = s.substring(7);
            //这里用逗号划分，uri中可能包含逗号从而出错
            String[] params = s.split(", ");
            Map<String, String> map = new HashMap<>(16);
            for (String param : params) {
                String[] kv = param.split("=", 2);
                map.put(kv[0].trim(), kv[1].trim().replaceAll("\"", ""));
            }
            return degestCheck(map, req.method().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //basic方式登录
    public static boolean basicLogin(HttpRequest req) {
        String s = req.headers().get(proxyHead);
        if (s == null) {
            return false;
        }
        try {
            String[] split = s.split(" ");
            byte[] decode = Base64.decodeBase64(split[1]);
            String userNamePassWord = new String(decode);
            String[] split1 = userNamePassWord.split(":", 2);
            return basicCheck(split1[0], split1[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static HttpResponse getDigestNotLoginResponse() {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
        resp.headers().add("Proxy-Authenticate", "Digest realm=\"apache\",nonce=\"" + System.nanoTime() + "\",qop=\"auth\"");
        resp.headers().setInt("Content-Length", resp.content().readableBytes());
        return resp;
    }


    public static HttpResponse getBasicNotLoginResponse() {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
        resp.headers().add("Proxy-Authenticate", "Basic realm=\"Text\"");
        resp.headers().setInt("Content-Length", resp.content().readableBytes());
        return resp;
    }


    //使用basic的方式判断账号密码对不对
    private static boolean basicCheck(String userName, String passWord) {
        return realUserName.equals(userName) && realPassword.equals(passWord);
    }

    //使用degest的方式验证密码
    private static boolean degestCheck(Map<String, String> map, String method) {
        String h1 = realUserName + ":" + map.get("realm") + ":" + realPassword;
        String h2 = method + ":" + map.get("uri");
        String s = md5(md5(h1) + ":" + map.get("nonce") + ":" + map.get("nc") + ":" + map.get("cnonce") + ":auth:" + md5(h2));
        return s.equals(map.get("response"));
    }

    private static String md5(String resource) {
        return DigestUtils.md5Hex(resource);
    }


}
