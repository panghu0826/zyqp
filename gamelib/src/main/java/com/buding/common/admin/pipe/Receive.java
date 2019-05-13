package com.buding.common.admin.pipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;

class Receive implements Runnable { // 实现Runnable接口 
    private PipedInputStream pis = null;
    public Receive() { 
        this.pis = new PipedInputStream(); // 实例化输入流 
    }
    public void run() {
        try { 
        	BufferedReader reader = new BufferedReader(new InputStreamReader(pis));
            String line = null;
            while((line = reader.readLine()) != null) {
            	System.out.println("接收的内容为：" + line);    	
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }         
    }
    
    public PipedInputStream getPis() { 
        return pis; 
    } 
}