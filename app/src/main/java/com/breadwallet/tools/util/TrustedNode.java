package com.breadwallet.tools.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TrustedNode {
    public static  String removePort(String input) {
        return input.startsWith("[")
                 ? input.split("]")[0]+"]"
                 : input.split(":")[0];
    }
    
    public static  String getNodeHost(String input) {
        try {
            return InetAddress.getByName(removePort(input)).getHostAddress().toString();
        } catch (UnknownHostException e) {
            return "Error";
        }
    }

    public static  int getNodePort(String input) {
        int port = 0;
        try {
            port = Integer.parseInt(input.split(":")[input.split(":").length - 1]);
        } catch (Exception e) {
        }
        return port;
    }

    public static  boolean isValid(String input) {
        return (isIPv4(input) || isIPv6(input));
    }

    public static  boolean isIPv4(String input) {
        try {
            InetAddress inetAddress = InetAddress.getByName(removePort(input));
            return (inetAddress instanceof Inet4Address) && inetAddress.getHostAddress().equals(removePort(input));
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    public static boolean isIPv6(String input) {
        try {
            InetAddress inetAddress = InetAddress.getByName(removePort(input));
            return inetAddress instanceof Inet6Address;
        } catch (UnknownHostException ex) {
            return false;
        }
    }
}
