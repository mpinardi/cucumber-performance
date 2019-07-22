package cucumber.perf.api;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @see Cucumber io.cucumber.core.model.FeaturePath.java
 */
public class URIPath {

    private URIPath() {

    }

    public static URI parse(String pathIdentifier) {
        if (nonStandardPathSeparatorInUse(pathIdentifier)) {
            String standardized = replaceNonStandardPathSeparator(pathIdentifier);
            return parseAssumeFileScheme(standardized);
        }

        if (probablyURI(pathIdentifier)) {
            return parseProbableURI(pathIdentifier);
        }

        return parseAssumeFileScheme(pathIdentifier);
    }

    private static URI parseProbableURI(String pathIdentifier) {
        return URI.create(pathIdentifier);
    }

    private static boolean probablyURI(String pathIdentifier) {
        return pathIdentifier.matches("^\\w+:.*$");
    }


    private static String replaceNonStandardPathSeparator(String pathIdentifier) {
        return pathIdentifier.replace(File.separatorChar, '/');
    }

    private static boolean nonStandardPathSeparatorInUse(String pathIdentifier) {
        return File.separatorChar != '/' && pathIdentifier.contains(File.separator);
    }

    private static URI parseAssumeFileScheme(String pathIdentifier) {
        File pathFile = new File(pathIdentifier);
        if (pathFile.isAbsolute()) {
            return pathFile.toURI();
        }

        try {
            URI root = new File("").toURI();
            URI relative = root.relativize(pathFile.toURI());
            // Scheme is lost by relativize
            return new URI("file", relative.getSchemeSpecificPart(), relative.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}