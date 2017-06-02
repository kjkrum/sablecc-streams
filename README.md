# SableCC Streams
A fork of SableCC 3.7 that can parse continuous streams containing a mixture of recognized patterns and garbage.

The license and related files are included in the source tree as resources because they can be displayed by the software. They can be found in the `org.sablecc.sablecc` package.

## How it works
Standard SableCC has a strict lexer that throws an exception when it encounters an unrecognized token, and a parser that expects a single terminal production followed immediately by an `EOF`. In this version, the lexer can emit `InvalidToken` and the parser skips unrecognized tokens and incomplete productions, backtracking and throwing out one character at a time to ensure nothing is missed. `LexerException` and `ParserException` are never thrown unless something is overridden to do so. (I may eliminate them completely before I consider this version finished.)

## Grammar
There is no longer any requirement that the grammar include an empty alternation of the terminal production. If you want garbage to be returned for debugging purposes, include a single-character garbage token and corresponding production.

```
Tokens
    junk = [32..127] ; /* Or whatever. */

Productions
    event = {foo} foo | {bar} bar | {junk} junk ;
```

If responsiveness is important, avoid productions that include repetition. Consider a language that accepts A+. A call to `parse()` would not return until the lexer emits `EOF` or `InvalidToken`. Even if an unrecognized token is eventually expected, the caller would not receive the first production until the unrecognized token arrives. If the language accepted A instead of A+, then each A would be returned immediately.

## Usage
A `Start` may contain a null production or a null `EOF`, but not both. The presence of a non-null `EOF` indicates the end of the stream.
```
while(true) {
    final Start start = parser.parse();
    start.apply(visitor);
    if(start.getEOF() != null) {
        break;
    }
}
```

## Maven
To use SableCC Streams in a Maven project, build with [sablecc-maven-plugin](https://github.com/johnny-bui/sablecc-maven-plugin) version 2.0-beta.6-SNAPSHOT or later.

```
<plugin>
	<groupId>com.github.verylazyboy</groupId>
	<artifactId>sablecc-maven-plugin</artifactId>
	<version>2.0-beta.6-SNAPSHOT</version>
	<configuration>
		<grammar>Example.sablecc</grammar>
	</configuration>
	<executions>
		<execution>
			<goals>
				<goal>sablecc</goal>
			</goals>
		</execution>
	</executions>
	<dependencies>
		<dependency>
			<groupId>com.chalcodes</groupId>
			<artifactId>sablecc-streams</artifactId>
			<version>3.7-SNAPSHOT</version>
		</dependency>
	</dependencies>
</plugin>
```

