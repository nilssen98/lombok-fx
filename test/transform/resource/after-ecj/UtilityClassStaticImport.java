package pkg;
import lombok.experimental.UtilityClass;
import static pkg.StaticImport.test;
public class UtilityClassStaticImport {
  public UtilityClassStaticImport() {
    super();
  }
  public void method() {
    test();
  }
}
final @UtilityClass class StaticImport {
  public static void test() {
  }
  private @java.lang.SuppressWarnings("all") StaticImport() {
    super();
    throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
