package com.buding.common.admin.pipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;

class Send implements Runnable { 
    // 实现Runnable接口 
    private PipedOutputStream pos = null; // 管道输出流
    public Send() { 
        this.pos = new PipedOutputStream();// 实例化输出流 
    }
    public void run() { 
        try { 
        	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while((line = reader.readLine()) != null) {
            	this.pos.write(line.getBytes()); // 输出信息 
            	this.pos.flush();
            	break;
            }
            this.pos.close(); // 关闭输出流 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
    }
    
    public PipedOutputStream getPos() { // 通过线程类得到输出流 
        return pos; 
    } 
}