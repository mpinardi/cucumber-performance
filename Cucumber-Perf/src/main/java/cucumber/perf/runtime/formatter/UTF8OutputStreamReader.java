package cucumber.perf.runtime.formatter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

class UTF8InputStreamReader extends InputStreamReader {
    UTF8InputStreamReader(InputStream in) {
        super(in, Charset.forName("UTF-8"));
    }
}