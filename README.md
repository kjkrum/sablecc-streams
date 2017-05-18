# SableCC Streams
A fork of SableCC 3.7 that can parse continuous streams containing a mixture of recognized patterns and garbage.

The license and related files are included in the source tree as resources because they can be displayed by the software. They can be found in the `org.sablecc.sablecc` package.

## How it works
Standard SableCC expects a single terminal production followed by an `EOF`. In this version, `EOF` can also represent the end of a production. When the lexer encounters an unrecognized token, it produces an `EOF` instead of throwing a `LexerException`. This is how the parser recognizes the end of a production followed by garbage. When the parser encounters an `ERROR` action, it tests what the action would be if the next token were `EOF`. If the hypothetical action is `ACCEPT`, the parser synthesizes an `EOF` and returns the production. This is how the parser recognizes the end of a production followed by a recognized token.

There are some limitations to this approach. See [Issues](#issues).

## Grammar
The grammar must include an empty alternation of the terminal production. The parser needs to be able to return an empty production if it reads `EOF` before reading a non-empty production.

```
Productions
    event = {foo} foo | {bar} bar | {empty} ;
```

If responsiveness is important, avoid productions that include repetition. Consider a language that accepts A+. A call to `parse()` would not return until the stream is closed or the lexer encounters an unrecognized token. Even if an unrecognized token is eventually expected, the caller would not receive the first production until the unrecognized token arrives. If the language accepted A instead of A+, then each A would be returned as it arrives.

## Usage
```
while(true) {
    final Start start = parser.parse();
    start.apply(visitor);
    if(!start.getEOF().isSynthetic()) {
        /* Actual end of stream. */
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

## Issues
I don't know enough about formal language theory to describe these things in proper terms, so I'll explain by way of example. These languages and inputs would not be accepted by standard SableCC. They *should* be accepted by this version, but aren't.

In each of these examples, the problem is that the parser can't backtrack. I may eventually implement this, but I'm putting it off until I need it.

Example 1

- Language accepts AB | C | ABCD
- Input is ABCX
- Parser should push the tokens of C back into the lexer, and return AB.

Example 2

- Language accepts BC | ABCD
- Input is ABCX
- Parser should push everything back into the lexer, throw out the first token of A, and see what lexes out.