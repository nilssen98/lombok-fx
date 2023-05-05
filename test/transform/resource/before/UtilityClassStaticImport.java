package pkg;
import lombok.experimental.UtilityClass;
import static pkg.StaticImport.test;

public class UtilityClassStaticImport {
	public void method() {
		test();
	}
}

@UtilityClass
class StaticImport {
	public void test() {
	}
}