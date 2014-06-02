package com.samueli.easyschedule.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class IpUtils {

    private static final Logger log = Logger.getLogger(IpUtils.class);

    public static String localIp() {
        String localIp = null;
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("get local host exception", e);
        }

        return localIp;
    }

}
