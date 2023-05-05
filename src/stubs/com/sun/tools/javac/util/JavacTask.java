package com.sun.tools.javac.util;

import com.sun.source.util.TaskListener;

public abstract class JavacTask {
	public abstract void addTaskListener(TaskListener taskListener);
}
