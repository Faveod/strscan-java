package com.faveod.strscan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A clone of Ruby's StringScanner from `strscan`.
 *
 * @version 1.0
 * @author <a href="mailto:firas.al-khalil@faveod.com">Firas al-Khalil</a>
 */
public class StringScanner {

    private final CharSequence charSequence;
    private final Map<String, Pattern> cachedPatterns;
    private final Supplier<Boolean> scanPredicate;
    private final Supplier<Boolean> scanUntilPredicate;
    private String matched;
    private Matcher matcher;
    private int pos;

    //
    // Constructors
    //

    public StringScanner(CharSequence str) {
        this.charSequence = str;
        this.cachedPatterns = new HashMap<>();
        this.scanPredicate = () -> this.matcher.lookingAt();
        this.scanUntilPredicate = () -> this.matcher.find();
        this.matched = null;
        this.pos = 0;
    }

    //
    // Getters and Setters
    //

    /**
     * Get the current position in the supplied {@link java.lang.CharSequence}.
     *
     * @return the current position in the supplied
     * {@link java.lang.CharSequence}.
     */
    public int getPos() { return pos; }

    /**
     * Set the position from which scanning should start.
     *
     * @param pos the position from which scanning starts.
     */
    public void setPos(int pos) { this.pos = pos; }


    //
    // Public API
    //

    /**
     * The list of captures from the previous scan.
     *
     * @return the subgroups in the most recent match (not including the full
     * match), or `null` if nothing was priorly matched.
     */
    public List<String> captures() {
        List<String> res = new ArrayList<>();
        if (matched != null) {
            for (var i = 0; i < matcher.groupCount(); ++i) {
                res.add(matcher.group(i + 1));
            }
        }
        return res;
    }

    /**
     * Get the underlying string.
     *
     * @return the underlying string.
     */
    public final String getString() {
        return charSequence.toString();
    }

    /**
     * Returns true if the scan pointer is at the end of the string.
     *
     * @return true if the provided {@link java.lang.CharSequence} is null or
     * if the current scanning position is equal to or greater than the
     * underlying {@link java.lang.CharSequence}'s length.
     */
    public boolean isEos() {
        return charSequence == null || pos >= charSequence.length();
    }

    /**
     * Returns the last matched string.
     *
     * @return the matched string.
     */
    public String matched() {
        return matched;
    }

    /**
     * Extracts a string corresponding to string[pos,len], without advancing
     * the scan pointer.
     *
     * @param len the length of the subsequence.
     * @return a subsequence of the underlying CharSequence.
     */
    public CharSequence peek(int len) {
        var endIndex = Math.max(Math.min(charSequence.length(), pos + len), pos);
        return charSequence.subSequence(pos, endIndex);
    }

    /**
     * Reset the scan pointer (index 0) and clear matching data.
     */
    public void reset() {
        matched = null;
        pos = 0;
    }

    /**
     * Tries to match with pattern at the current position.
     *
     * The {@link java.util.regex.Pattern} created from the supplied
     * {@link java.lang.String} will be cached internally, so do not hesitate
     * to use this API.
     *
     * @param regex the pattern.
     * @return if there's a match, the scanner advances the "scan pointer" and
     * returns the matched string.  Otherwise, the scanner returns null.
     * @see StringScanner#scan(Pattern)
     */
    public String scan(String regex) {
        return scan(loadPattern(regex));
    }

    /**
     * Tries to match with pattern at the current position.
     *
     * @param regex the pattern.
     * @return if there's a match, the scanner advances the “scan pointer” and
     * returns the matched string.  Otherwise, the scanner returns null.
     */
    public String scan(Pattern regex) {
        return scanImpl(regex, scanPredicate);
    }

    /**
     * Scans the string until the pattern is matched.
     *
     * The {@link java.util.regex.Pattern} created from the supplied
     * {@link java.lang.String} will be cached internally, so do not hesitate
     * to use this API.
     *
     * @param regex the pattern.
     * @return the substring up to and including the end of the match,
     * advancing the scan pointer to that location. If there is no match,
     * null is returned.
     * @see StringScanner#scanUntil(Pattern)
     */
    public String scanUntil(String regex) {
        return scanUntil(loadPattern(regex));
    }

    /**
     * Scans the string until the pattern is matched.
     *
     * @param regex the pattern.
     * @return the substring up to and including the end of the match,
     * advancing the scan pointer to that location. If there is no match,
     * null is returned.
     */
    public String scanUntil(Pattern regex) {
        return scanImpl(regex, scanUntilPredicate);
    }

    /**
     * Sets the scan pointer to the end of the string and clear matching data.
     */
    public void terminate() {
        pos = charSequence != null ? charSequence.length() : 0;
        matched = null;
    }

    //
    // Private methods
    //

    private Pattern loadPattern(String regex) {
        Pattern pattern = null;
        if (cachedPatterns.containsKey(regex)) {
            pattern = cachedPatterns.get(regex);
        } else {
            pattern = Pattern.compile(regex);
            cachedPatterns.put(regex, pattern);
        }
        return pattern;
    }

    private void mintMatcher(Pattern regex) {
        if (matcher == null && charSequence != null) {
            matcher = regex.matcher(charSequence);
        } else if (matcher != null) {
            matcher.usePattern(regex);
        }
    }

    // A sad state that is forced upon us by the Gods of Java.
    // A sad state that there's no templates.
    private String scanImpl(Pattern regex, Supplier<Boolean> predicate) {
        mintMatcher(regex);
        if (charSequence != null) {
            matcher.region(pos, charSequence.length());
            if (predicate.get()) {
                matched = matcher.group(0);
                pos = matcher.end();
            } else {
                matched = null;
            }
        }
        return matched;
    }
}
