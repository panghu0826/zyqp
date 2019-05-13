package com.buding.test;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class Card {
	public String name;
	public int code;
	
	public Card(int code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String toString() {
		return name+":"+code;
	}
}
