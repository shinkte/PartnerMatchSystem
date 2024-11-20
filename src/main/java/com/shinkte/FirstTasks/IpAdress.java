package com.shinkte.FirstTasks;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAdress {

    public static void main(String[] args) {
        String str = "wss://weixintongtech.cn/m4aWs/wss/serial/111";
        System.out.println(str.split("/")[6]);
        System.out.println("=========================");
    }
}
