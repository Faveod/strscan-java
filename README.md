# StringScanner

[![Java CI](https://github.com/Faveod/strscan-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Faveod/strscan-java/actions/workflows/ci.yml)

This is a clone of [Ruby's `StringScanner`](https://docs.ruby-lang.org/en/3.2/StringScanner.html) from `strscan`.

It's not feature complete, but it has all the minimum scanning features
expected.

## Examples

### `scan`

Tries to match with pattern at the current position. If there's a match, the scanner advances the “scan pointer” and
returns the matched string. Otherwise, the scanner returns `null`.

```java
import com.faveod.strscan.StringScanner;
import java.util.regex.Pattern;

var words = Pattern.compile("\\w+");
var spaces = Pattern.compile("\\s+");
var s = new StringScanner("This is an example string");

s.isEos();         // -> false

s.scan(words);     // -> "This"
s.scan(words);     // -> nil
s.scan(spaces);    // -> " "
s.scan(spaces);    // -> nil
s.scan(words);     // -> "is"
s.isEos();         // -> false

s.scan(spaces);    // -> " "
s.scan(words);     // -> "an"
s.scan(spaces);    // -> " "
s.scan(words);     // -> "example"
s.scan(spaces);    // -> " "
s.scan(words);     // -> "string"
s.isEos();         // -> true

s.scan(spaces);    // -> nil
s.scan(words);     // -> nil
```

### `scanUntil`

Scans the string until the pattern is matched. Returns the substring up to and including the end of the match, advancing
the scan pointer to that location. If there is no match, `null` is returned.

```java
import com.faveod.strscan.StringScanner;
import java.util.regex.Pattern;

var s = new StringScanner("Fri Dec 12 1975 14:39");
s.scanUntil("1");  // -> "Fri Dec 1"
s.getPos();        // -> 9
```