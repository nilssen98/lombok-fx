import java.util.List;
class BuilderConstructorJavadoc<T> {
  public static class BuilderConstructorJavadocBuilder<T> {
    private @java.lang.SuppressWarnings("all") int basic;
    private @java.lang.SuppressWarnings("all") int multiline;
    private @java.lang.SuppressWarnings("all") int predef;
    private @java.lang.SuppressWarnings("all") int predefWithJavadoc;
    private @java.lang.SuppressWarnings("all") int last;
    public BuilderConstructorJavadocBuilder<T> predef(final int x) {
      this.predef = x;
      return this;
    }
    public BuilderConstructorJavadocBuilder<T> predefWithJavadoc(final int x) {
      this.predefWithJavadoc = x;
      return this;
    }
    @java.lang.SuppressWarnings("all") BuilderConstructorJavadocBuilder() {
      super();
    }
    /**
     * @param basic tag is moved to the setter
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> basic(final int basic) {
      this.basic = basic;
      return this;
    }
    /**
     * @param multiline a param comment
     *        can be on multiple lines and can use 
     *        {@code @code} or <code>tags</code>
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> multiline(final int multiline) {
      this.multiline = multiline;
      return this;
    }
    /**
     * @param last also copy last param
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> last(final int last) {
      this.last = last;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderConstructorJavadoc<T> build() {
      return new BuilderConstructorJavadoc<T>(this.basic, this.multiline, this.predef, this.predefWithJavadoc, this.last);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((((((("BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder(basic=" + this.basic) + ", multiline=") + this.multiline) + ", predef=") + this.predef) + ", predefWithJavadoc=") + this.predefWithJavadoc) + ", last=") + this.last) + ")");
    }
  }
  @lombok.Builder BuilderConstructorJavadoc(int basic, int multiline, int predef, int predefWithJavadoc, int last) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> builder() {
    return new BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T>();
  }
}