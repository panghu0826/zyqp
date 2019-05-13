package com.buding.common.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class AdminClient {
	public static void main(String[] args) throws Exception {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(9984));
			final InputStream sockInp = socket.getInputStream();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(sockInp, "utf8"));
			final PrintWriter writer = new PrintWriter(socket.getOutputStream());
			
			
			final BufferedReader cmdReader = new BufferedReader(new InputStreamReader(System.in, "gbk"));		
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						String line = null;
						while((line = reader.readLine()) != null) {
							System.out.println(line);
							System.out.flush();
						}		
						System.out.println("ServerClose()");
					} catch (IOException e) {					
						e.printStackTrace();
					}
				}
			}).start();
			
			new Thread(new Runnable() {			
				@Override
				public void run() {
					try {
						String line = null;
						while((line = cmdReader.readLine()) != null) {
							System.out.println("CMD:" + line);
							writer.println(line);
							writer.flush();
							if("exit".equals(line)) {
								System.exit(0);
							}
						}
					} catch (IOException e) {					
						e.printStackTrace();
					}
				}
			}).start();
		}
}

