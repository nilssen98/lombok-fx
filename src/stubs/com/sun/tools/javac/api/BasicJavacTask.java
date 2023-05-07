package com.sun.tools.javac.api;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.util.Context;

public abstract class BasicJavacTask extends JavacTask {
	public static JavacTask instance(Context context) { return null; }
	public Context getContext() { return null; }
	public void addTaskListener(TaskListener taskListener) { }
}
