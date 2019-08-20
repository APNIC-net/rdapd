package net.apnic.rdapd.rpsl;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Macros are comments added to RPSL objects that provides hints to the RDAP object generation.
 */
public enum Macro {
    NO_VCARD("^NO_VCARD"), LINK("^LINK");


    private Pattern pattern;

    private Macro(String regex) {
       pattern = Pattern.compile(regex);
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

    public static Optional<Properties> getProperties(String comment, Macro macro) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public static List<Properties> getProperties(Iterable<String> comments, Macro macro) {
        return StreamSupport.stream(comments.spliterator(), false)
                .map(c -> Macro.getProperties(c, macro))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
