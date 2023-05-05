package lombok.javac;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;

import lombok.permit.Permit;

public class LombokLog extends Log {
	
	protected LombokLog(Context context) {
		super(context);
	}
	
	public static LombokLog instance(Context context) {
		Log log = context.get(logKey);
		if (log instanceof LombokLog) {
			return (LombokLog) log;
		}
		// Replace original Log with LombokLog
		context.put(logKey, (Log) null);
		LombokLog lombokLog = new LombokLog(context);
		replaceInContextObjects(context, lombokLog);
		
		return lombokLog;
	}
	
	private static void replaceInContextObjects(Context context, LombokLog lombokLog) {
		Field htField = Permit.permissiveGetField(Context.class, "ht");
		try {
			Map<?, ?> contextMap = (Map<?, ?>) Permit.get(htField, context);
			for (Object contextObj : contextMap.values()) {
				Field[] fields = contextObj.getClass().getDeclaredFields();
				for (Field field : fields) {
					if (field.getType() != Object.class && field.getType().isAssignableFrom(Log.class)) {
						Permit.setAccessible(field);
						Permit.set(field, contextObj, lombokLog);
					}
				}
			}
		} catch (Exception e) {
			// Ignore
		}
	}
	
	@Override
	public void report(JCDiagnostic diagnostic) {
		Reflect.markAsRecoverable(diagnostic);
		
		super.report(diagnostic);
	}
	
	private static class Reflect {
		private static final Field FLAGS = Permit.permissiveGetField(JCDiagnostic.class, "flags");
		private static final Object RECOVERABLE;
		static {
			Object r = null;
			try {
				r = Class.forName("com.sun.tools.javac.util.JCDiagnostic$DiagnosticFlag").getField("RECOVERABLE").get(null);
			} catch (Exception e) {
				// Ignore
			}
			RECOVERABLE = r;
		}
		
		private static void markAsRecoverable(JCDiagnostic diagnostic) {
			if (FLAGS == null || RECOVERABLE == null) return;
			try {
				Set<Object> flags = (Set<Object>) Permit.get(FLAGS, diagnostic);
				flags.add(RECOVERABLE);
			} catch (Exception e) {
				// Ignore
			}
		}
	}
}
