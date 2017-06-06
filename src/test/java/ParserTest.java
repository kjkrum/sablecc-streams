import org.junit.Test;
import test.analysis.AnalysisAdapter;
import test.analysis.DepthFirstAdapter;
import test.lexer.Lexer;
import test.lexer.LexerException;
import test.node.*;
import test.parser.Parser;
import test.parser.ParserException;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Parser tests.
 *
 * @author Kevin Krumwiede
 */
public class ParserTest {

	@Test
	public void testAcceptWholeInput() {
		final Parser parser = newParser("abcd");
		try {
			final Start result = parser.parse();
			final boolean[] visited = new boolean[1];
			result.apply(new DepthFirstAdapter() {
				@Override
				public void caseAAbcdAlt(final AAbcdAlt node) {
					visited[0] = true;
				}
			});
			assertTrue(visited[0]);
			assertNotNull(result.getEOF());
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testBacktracking() {
		final Parser parser = newParser("abcx");
		try {
			final boolean[] visited = new boolean[1];
			final Start result1 = parser.parse();
			result1.apply(new DepthFirstAdapter() {
				@Override
				public void caseAAbAlt(final AAbAlt node) {
					visited[0] = true;
				}
			});
			assertTrue(visited[0]);
			assertNull(result1.getEOF());

			// read past the 'x' to EOF
			final Start result2 = parser.parse();
			assertNull(result2.getPAlt());
			assertNotNull(result2.getEOF());
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private static Parser newParser(final String input) {
		return new Parser(new Lexer(new PushbackReader(new StringReader(input), Math.max(1, input.length()))));
	}
}
