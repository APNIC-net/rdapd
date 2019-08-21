package net.apnic.rdapd.rpsl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Macros are comments added to RPSL objects that provides hints to the RDAP object generation.
 */
public enum Macro {
    NO_VCARD("^NO_VCARD", false), LINK("(?:^LINK: )(.*)", true);

    private Pattern pattern;
    private boolean containProperties;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(Macro.class);

    Macro(String regex, boolean containProperties) {
       pattern = Pattern.compile(regex);
       this.containProperties = containProperties;
    }

    public Pattern getPattern() {
       return pattern;
    }

    public static Optional<Macro> getMacroForComment(String comment) {
        return Arrays.stream(values()).filter(m -> m.pattern.matcher(comment).matches()).findFirst();
    }

    public static Set<Macro> getMacrosForComments(Iterable<String> comments) {
        return StreamSupport.stream(comments.spliterator(), false)
                .map(Macro::getMacroForComment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public static boolean matches(String comment, Macro macro) {
        return macro.pattern.matcher(comment).matches();
    }

    public static boolean anyMatches(Iterable<String> comments, Macro macro) {
        return StreamSupport.stream(comments.spliterator(), false)
                .anyMatch(c -> matches(c, macro));
    }

    public static Optional<Properties> getMacroProperties(String comment, Macro macro) {
        if (!macro.containProperties) {
            return Optional.empty();
        }

        Matcher matcher = macro.pattern.matcher(comment);
        JsonNode jsonNode;

        if (!matcher.find()) {
            return Optional.empty();
        }

        try {
            jsonNode = OBJECT_MAPPER.readTree(matcher.group(1));
        } catch (IOException e) {
            LOGGER.warn(String.format("Error trying to parse macro parameters. Macro: %s , parameters: %s",
                    macro, matcher.group()), e);
            return Optional.empty();
        }

        Properties props = new Properties();
        jsonNode.fields().forEachRemaining(e -> props.put(e.getKey(), e.getValue().asText()));
        return Optional.of(props);
    }

    public static List<Properties> getMacroProperties(Iterable<String> comments, Macro macro) {
        return StreamSupport.stream(comments.spliterator(), false)
                .map(c -> Macro.getMacroProperties(c, macro))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public enum LinkProperties {
        REL("rel"), TYPE("type"), HREF("href"), HREFLANG("hreflang");

        private String name;

        LinkProperties(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
