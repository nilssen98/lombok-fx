package pkg;

import static pkg.StaticImport.test;

public class UtilityClassStaticImport {
	public void method() {
		test();
	}
}

final class StaticImport {
	public static void test() {
	}

	@java.lang.SuppressWarnings("all")
	private StaticImport() {
		throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
