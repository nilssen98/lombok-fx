package lombok.launch;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;

public class CompilerPlugin implements Plugin {

	@Override
	public String getName() {
		return "Lombok";
	}

	@Override
	public void init(JavacTask task, String... args) {
		BasicJavacTask basicJavacTask = (BasicJavacTask) task;
		Context context = basicJavacTask.getContext();
		try {
			ClassLoader cl = Main.getShadowClassLoader();
			
			Class<?> processor = cl.loadClass("lombok.javac.apt.LombokProcessor");
			processor.getDeclaredMethod("addOpensForLombok").invoke(null);
			
			final Class<?> log = cl.loadClass("lombok.javac.LombokLog");
			final Object lombokLog = log.getDeclaredMethod("instance", Context.class).invoke(null, context);
			
			basicJavacTask.addTaskListener(new TaskListener() {
				
				@Override
				public void started(TaskEvent event) {
					if (event.getKind() == TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND) {
						try {
							log.getDeclaredMethod("stopDefer").invoke(lombokLog);
						} catch (Exception e) {
							// Ignore
						}
					}
				}
				
				@Override
				public void finished(TaskEvent event) {
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Error initializing lombok javac plugin");
		}
	}
	
}
