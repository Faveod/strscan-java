package com.faveod.test.strscan;

import com.faveod.strscan.StringScanner;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestStringScanner {
    private void assertAtResetState(StringScanner s) {
        assertNull(s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(0, s.getPos());
    }

    private void assertAtTerminatedState(StringScanner s) {
        assertNull(s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(s.getString().length(), s.getPos());
        assertTrue(s.isEos());
    }
    private void reset(StringScanner s) {
        s.reset();
        assertAtResetState(s);
    }

    @Test
    void peek() {
        var in = "test st";
        var s = new StringScanner(in);
        assertEquals("test st", s.peek(7));
        assertEquals("test st", s.peek(7));
        assertEquals("", s.peek(0));
        assertEquals(in, s.peek(in.length()));
        assertEquals(in, s.peek(in.length() + 1));

        s.terminate();
        assertAtTerminatedState(s);
        assertEquals("", s.peek(0));
        assertEquals("", s.peek(1));
        assertEquals("", s.peek(10));
    }

    @Test
    void terminate() {
        var in = "test st";
        var s = new StringScanner(in);
        s.scan("t.st");
        assertEquals("test", s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(4, s.getPos());

        s.terminate();
        assertAtTerminatedState(s);
    }

    private void trivialTests(String in) {
        var s = new StringScanner(in);
        assertTrue(s.isEos());
        assertAtResetState(s);

        reset(s);

        assertTrue(s.isEos());
        assertAtResetState(s);

        s.scan("hello");
        assertNull(s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(0, s.getPos());

        s.scanUntil("hello");
        assertNull(s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(0, s.getPos());
    }
    @Test
    void trivialScans() {
        trivialTests("");
        trivialTests(null);
    }

    @Test
    void simpleScan() {
        var in = "test st";
        var s = new StringScanner(in);
        // Match the first token
        s.scan("t.st");
        assertEquals("test", s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(4, s.getPos());

        // No match
        s.scan("not here");
        assertNull(s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(4, s.getPos());

        reset(s);
    }

    @Test
    void groupedScan() {
        var in = "test st";
        var s = new StringScanner(in);
        // Match the first token
        s.scan("(t.)st");
        assertEquals("test", s.matched());
        assertEquals(List.of("te"), s.captures());
        assertEquals(4, s.getPos());

        // Match the second token
        s.scan("\\s(s)(t)");
        assertEquals(" st", s.matched());
        assertEquals(Arrays.asList("s", "t"), s.captures());
        assertEquals(7, s.getPos());

        assertTrue(s.isEos());
        s.terminate();
        assertAtTerminatedState(s);
    }

    @Test
    void simpleScanAndSetPos() {
        var in = "test st";
        var s = new StringScanner(in);
        // Match the first token
        s.scan("est");
        assertNull(s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(0, s.getPos());

        s.setPos(1);
        s.scan("est");
        assertEquals("est", s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(4, s.getPos());
    }

    @Test
    void scanResetThenRescan() {
        var in = "test st";
        var s = new StringScanner(in);
        // Match the first token
        s.scan("(t.)st");
        assertEquals("test", s.matched());
        assertEquals(List.of("te"), s.captures());
        assertEquals(4, s.getPos());

        // Match the second token
        s.scan("\\s(s)(t)");
        assertEquals(" st", s.matched());
        assertEquals(Arrays.asList("s", "t"), s.captures());
        assertEquals(7, s.getPos());

        assertTrue(s.isEos());
        reset(s);

        // Match the first token
        s.scan("(t.)st");
        assertEquals("test", s.matched());
        assertEquals(List.of("te"), s.captures());
        assertEquals(4, s.getPos());

        reset(s);
        // No Match
        s.scan("bingo");
        assertNull(s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(0, s.getPos());
    }

    @Test
    void simpleScanUntil() {
        var in = "test st";
        var s = new StringScanner(in);
        // Match the first token
        s.scanUntil("st");
        assertEquals("st", s.matched());
        assertEquals(List.of(), s.captures());
        assertEquals(4, s.getPos());

        s.terminate();
        assertAtTerminatedState(s);
    }

    @Test
    void groupedScanUntil() {
        var in = "test st";
        var s = new StringScanner(in);
        // Match the first token
        s.scanUntil("(st).*(st)");
        assertEquals("st st", s.matched());
        assertEquals(Arrays.asList("st", "st"), s.captures());
        assertEquals(7, s.getPos());

        assertTrue(s.isEos());
        s.terminate();
        assertAtTerminatedState(s);
    }

    @Test
    void exampleFromRubyDocs() {
        var words = Pattern.compile("\\w+");
        var spaces = Pattern.compile("\\s+");
        var s = new StringScanner("This is an example string");

        assertFalse(s.isEos());

        assertEquals(s.scan(words), "This");
        assertNull(s.scan(words));
        assertEquals(s.scan(spaces), " ");
        assertNull(s.scan(spaces));
        assertEquals(s.scan(words), "is");
        assertFalse(s.isEos());

        assertEquals(s.scan(spaces), " ");
        assertEquals(s.scan(words), "an");
        assertEquals(s.scan(spaces), " ");
        assertEquals(s.scan(words), "example");
        assertEquals(s.scan(spaces), " ");
        assertEquals(s.scan(words), "string");
        assertTrue(s.isEos());

        assertNull(s.scan(spaces));
        assertNull(s.scan(words));
    }
}
