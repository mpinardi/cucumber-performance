package cucumber.perf.runtime.formatter;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import io.cucumber.core.internal.gherkin.deps.com.google.gson.Gson;

/**
 * A stream that can read from both file and http URLs. If it's a file URL, writes with a {@link java.io.FileInputStream},
 * if it's a http or https URL, writes with a HTTP PUT (by default) or with the specified method.
 */
class URLInputStream extends InputStream {
    private final URL url;
    private final String method;
    private final int expectedResponseCode;
    private final InputStream in;
    private final HttpURLConnection urlConnection;

    URLInputStream(URL url) throws IOException {
        this(url, "PUT", Collections.<String, String>emptyMap(), 200);
    }

    private URLInputStream(URL url, String method, Map<String, String> headers, int expectedResponseCode) throws IOException {
        this.url = url;
        this.method = method;
        this.expectedResponseCode = expectedResponseCode;
        if (url.getProtocol().equals("file")) {
            File file = new File(url.getFile());
            ensureParentDirExists(file);
            in = new FileInputStream(file);
            urlConnection = null;
        } else if (url.getProtocol().startsWith("http")) {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setDoInput(true);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
            in = urlConnection.getInputStream();
        } else {
            throw new IllegalArgumentException("URL Scheme must be one of file,http,https. " + url.toExternalForm());
        }
    }

    private void ensureParentDirExists(File file) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().isDirectory()) {
            throw new IOException("File does not exist at " + file.getParentFile().getAbsolutePath());
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        return in.read(buffer, offset, count);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return in.read(buffer);
    }

	@Override
	public int read() throws IOException {
		return in.read();
	}
	
	 @Override
	    public void close() throws IOException {
	        try {
	            if (urlConnection == null) {
	                return;
	            }

	            int responseCode = urlConnection.getResponseCode();
	            if (responseCode == expectedResponseCode) {
	                return;
	            }

	            try {
	                urlConnection.getInputStream().close();
	                throw new IOException(String.format("Expected response code: %d. Got: %d", expectedResponseCode, responseCode));
	            } catch (IOException expected) {
	                InputStream errorStream = urlConnection.getErrorStream();
	                if (errorStream != null) {
	                    throw createResponseException(responseCode, expected, errorStream);
	                } else {
	                    throw expected;
	                }
	            }
	        } finally {
	            in.close();
	        }
	    }

	    private ResponseException createResponseException(int responseCode, IOException expected, InputStream errorStream) throws IOException {
	        try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, UTF_8))) {
	            String responseBody = br.lines().collect(Collectors.joining(System.lineSeparator()));
	            String contentType = urlConnection.getHeaderField("Content-Type");
	            if (contentType == null) {
	                contentType = "text/plain";
	            }
	            return new ResponseException(responseBody, expected, responseCode, contentType);
	        }
	    }

	    @SuppressWarnings("serial")
		class ResponseException extends IOException {
	        private final Gson gson = new Gson();
	        private final int responseCode;
	        private final String contentType;

	        public ResponseException(String responseBody, IOException cause, int responseCode, String contentType) {
	            super(responseBody, cause);
	            this.responseCode = responseCode;
	            this.contentType = contentType;
	        }

	        @Override
	        public String getMessage() {
	            if (contentType.equals("application/json")) {
	                Map<?,?> map = gson.fromJson(super.getMessage(), Map.class);
	                if (map.containsKey("error")) {
	                    return getMessage0(map.get("error").toString());
	                } else {
	                    return getMessage0(super.getMessage());
	                }
	            } else {
	                return getMessage0(super.getMessage());
	            }
	        }

	        private String getMessage0(String message) {
	            return String.format("%s %s\nHTTP %d\n%s", method, url, responseCode, message);
	        }
	    }
}
