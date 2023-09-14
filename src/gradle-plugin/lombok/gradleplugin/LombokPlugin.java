package lombok.gradleplugin;

import java.util.Arrays;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.compile.JavaCompile;

public class LombokPlugin implements Plugin<Project> {
	private static final List<String> OPENS = Arrays.asList(
		"--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
	);

	public void apply(Project target) {
		target.getTasks().withType(JavaCompile.class).configureEach(new Action<JavaCompile>() {
			public void execute(final JavaCompile compileJava) {
				compileJava.doFirst("Opens modules for lombok", new Action<Task>() {
					public void execute(Task t) {
						compileJava.getOptions().setFork(true);
						compileJava.getOptions().getForkOptions().getJvmArgs().addAll(OPENS);
					}
				});
			}
		});
	}
}