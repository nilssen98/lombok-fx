package lombok.launch;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
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
			
			Class<?> log = cl.loadClass("lombok.javac.LombokLog");
			log.getDeclaredMethod("instance", Context.class).invoke(null, context);
		} catch (Exception e) {
			throw new RuntimeException("Error initializing lombok javac plugin");
		}
	}
	
}
