package com.buding.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class IOUtil {
	public static String getClassPathResourceAsString(String path, String encoding) throws Exception {
		InputStream stream = IOUtil.class.getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
	
	public static byte[] tryGetFileData(String filePath) throws Exception {
		if(new File(filePath).exists()) {
			return getFileData(filePath);
		}
		return null;
	}
	
	public static byte[] getFileData(String filePath) throws Exception {
		FileInputStream fin = new FileInputStream(filePath);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			int size = 1024;
			byte buff[] = new byte[size];
			while((size = fin.read(buff)) != -1) {
				out.write(buff, 0, size);
			}
		} finally {
			fin.close();
		}
		
		return out.toByteArray();
	}
	
	public static String getFileResourceAsString(File file, String encoding) throws Exception {
		InputStream stream = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null) {
			sb.append(line).append("\r\n");
		}
		return sb.toString();
	}
	
	public static String getFileResourceAsString(String file, String encoding) throws Exception {
		return getFileResourceAsString(new File(file), encoding);
	}
	
	public static File[] listFiles(String folder) {
		File f = new File(folder);
		return f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				//不要隐藏文件
				return !pathname.getName().startsWith(".");
			}
		});
	}
	
	public static void writeFileContent(String filePath, String content) throws Exception {
		File file = new File(filePath);
		if(file.getParentFile() != null && file.getParentFile().exists() == false) {
			file.getParentFile().mkdirs();
		}
		FileOutputStream fout = new FileOutputStream(filePath);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
		writer.println(content);
		writer.flush();
		writer.close();
		return;
	}
}
