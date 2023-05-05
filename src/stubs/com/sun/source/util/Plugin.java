package com.sun.source.util;

public interface Plugin {
	String getName();
	
	void init(JavacTask task, String... args);
}
