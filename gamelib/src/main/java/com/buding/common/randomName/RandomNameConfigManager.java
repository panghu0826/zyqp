package com.buding.common.randomName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.buding.common.util.IOUtil;;

public class RandomNameConfigManager implements InitializingBean {
	public String nameConfigFolder;
	
	public List<Character> firstNames = new ArrayList<Character>();
	public List<Character> manMiddleNams = new ArrayList<Character>();
	public List<Character> manLastNames = new ArrayList<Character>();
	public List<Character> womenMiddleNames = new ArrayList<Character>();
	public List<Character> womenLastNames = new ArrayList<Character>();

	@Override
	public void afterPropertiesSet() throws Exception {
		if (nameConfigFolder != null) {
			for (File file : IOUtil.listFiles(nameConfigFolder)) {
				InputStream stream = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf8"));
				String line = null;
				while((line = reader.readLine()) != null) {
					String items[] = line.split(":");
					if(items.length == 1) {
						continue;
					}
					if(items[0].length() == 2) {
						Character fname = items[0].charAt(0);
						Character lname = items[0].charAt(1);
						firstNames.add(fname);
						if(items[1].equals("男")) {
							manLastNames.add(lname);
						} else {
							womenLastNames.add(lname);
						}
					} else if(items[0].length() == 3) {
						Character fname = items[0].charAt(0);
						Character mname = items[0].charAt(1);
						Character lname = items[0].charAt(2);
						firstNames.add(fname);
						
						if(items[1].equals("男")) {
							manMiddleNams.add(mname);
							manLastNames.add(lname);
						} else {
							womenMiddleNames.add(mname);
							womenLastNames.add(lname);
						}
					}
				}
			}
		}
	}
	
	public String getFirstName() {
		int ind = (int)(System.currentTimeMillis()%firstNames.size());
		return firstNames.get(ind).toString();
	}
	
	public String getManMidName() {
		int ind = (int)(System.currentTimeMillis()%manMiddleNams.size());
		return manMiddleNams.get(ind).toString();
	}
	
	public String getManLastName() {
		int ind = (int)(System.currentTimeMillis()%manLastNames.size());
		return manLastNames.get(ind).toString();
	}
	
	public String getWomenMidName() {
		int ind = (int)(System.currentTimeMillis()%womenMiddleNames.size());
		return womenMiddleNames.get(ind).toString();
	}
	
	public String getWomenLastName() {
		int ind = (int)(System.currentTimeMillis()%womenLastNames.size());
		return womenLastNames.get(ind).toString();
	}

	public String getNameConfigFolder() {
		return nameConfigFolder;
	}

	public void setNameConfigFolder(String nameConfigFolder) {
		this.nameConfigFolder = nameConfigFolder;
	}
}
