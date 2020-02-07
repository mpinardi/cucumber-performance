package cucumber.perf.api.formatter;

public class AnsiEscapes {
    private static final char ESC = 27;
    private static final char BRACKET = '[';

    public static final AnsiEscapes RESET = color(0);
    public static final AnsiEscapes BLACK = color(30);
    public static final AnsiEscapes RED = color(31);
    public static final AnsiEscapes GREEN = color(32);
    public static final AnsiEscapes YELLOW = color(33);
    public static final AnsiEscapes BLUE = color(34);
    public static final AnsiEscapes MAGENTA = color(35);
    public static final AnsiEscapes CYAN = color(36);
    public static final AnsiEscapes WHITE = color(37);
    public static final AnsiEscapes DEFAULT = color(9);
    public static final AnsiEscapes GREY = color(90);
    public static final AnsiEscapes INTENSITY_BOLD = color(1);

    private static AnsiEscapes color(int code) {
        return new AnsiEscapes(code + "m");
    }

    static AnsiEscapes up(int count) {
        return new AnsiEscapes(count + "A");
    }

    private final String value;

    private AnsiEscapes(String value) {
        this.value = value;
    }

    void appendTo(NiceAppendable a) {
        a.append(ESC).append(BRACKET).append(value);
    }

    void appendTo(StringBuilder a) {
        a.append(ESC).append(BRACKET).append(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendTo(sb);
        return sb.toString();
    }
}
