public class ExtensionMethodGlobal {
  static class Extensions {
    Extensions() {
      super();
    }
    public static <T>T or(T obj, T ifNull) {
      return ((obj != null) ? obj : ifNull);
    }
    public static String toTitleCase(String in) {
      if (in.isEmpty())
          return in;
      return (("" + Character.toTitleCase(in.charAt(0))) + in.substring(1).toLowerCase());
    }
  }
  public ExtensionMethodGlobal() {
    super();
  }
  public String test() {
    int[] intArray = {5, 3, 8, 2};
    java.util.Arrays.sort(intArray);
    String iAmNull = null;
    return ExtensionMethodGlobal.Extensions.or(iAmNull, ExtensionMethodGlobal.Extensions.toTitleCase("hELlO, WORlD!"));
  }
}
