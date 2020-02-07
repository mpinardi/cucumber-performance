package cucumber.perf.salad;


import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.cucumber.core.internal.gherkin.ast.Location;
import io.cucumber.core.internal.gherkin.deps.com.google.gson.Gson;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

@SuppressWarnings("unchecked")
public class SaladDialectProvider implements ISaladDialectProvider {
    private static Map<String, Map<String, List<String>>> DIALECTS;
    private final String default_dialect_name;

    static {
        Gson gson = new Gson();
        try {
            Reader dialects = new InputStreamReader(SaladDialectProvider.class.getResourceAsStream("/salad-language.json"), "UTF-8");
            DIALECTS = gson.fromJson(dialects, Map.class);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public SaladDialectProvider(String default_dialect_name) {
        this.default_dialect_name = default_dialect_name;
    }

    public SaladDialectProvider() {
        this("en");
    }

    public SaladDialect getDefaultDialect() {
        return getDialect(default_dialect_name, null);
    }

    @Override
    public SaladDialect getDialect(String language, Location location) {
        Map<String, List<String>> map = DIALECTS.get(language);
        if (map == null) {
            throw new ParserException.NoSuchLanguageException(language, location);
        }

        return new SaladDialect(language, map);
    }

    @Override
    public List<String> getLanguages() {
        List<String> languages = new ArrayList<String>(DIALECTS.keySet());
        sort(languages);
        return unmodifiableList(languages);
    }
}
