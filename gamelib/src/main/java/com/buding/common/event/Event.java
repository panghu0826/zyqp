package com.buding.common.event;

public class Event<T> {
	private String name;
	private T body;

	public static <T> Event<T> valueOf(String name, T body) {
		return new Event<T>(name, body);
	}

	public Event(String name) {
		this.name = name;
	}

	public Event(String name, T body) {
		this.name = name;
		this.body = body;
	}

	public String getName() {
		return this.name;
	}

	public T getBody() {
		return this.body;
	}

	public void setBody(T body) {
		this.body = body;
	}
}