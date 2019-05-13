package com.buding.common.admin.pipe;

import java.io.IOException;

public class TestPipe {
    public static void main(String[] args) {
        Send s = new Send(); 
        Receive r = new Receive(); 
        try { 
            s.getPos().connect(r.getPis()); // 连接管道 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
        new Thread(s).start(); // 启动线程 
        new Thread(r).start(); // 启动线程 
    } 
}