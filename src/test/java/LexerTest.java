import org.junit.Test;
import test.lexer.Lexer;
import test.lexer.LexerException;
import test.node.EOF;
import test.node.Token;
import test.parser.Parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Deque;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Lexer tests.
 *
 * @author Kevin Krumwiede
 */
public class LexerTest {
	@Test
	public void testPushback() {
		final String input = "abc";
		final Lexer lexer = newLexer(input);
		final Deque<Token> tokens = new LinkedList<Token>();

		try {
			// read
			tokens.push(lexer.next());          // a
			tokens.push(lexer.next());          // b

			// push back
			lexer.pushBack(tokens.pop());       // b
			lexer.pushBack(tokens.pop());       // a

			// read and concatenate
			final String ouput =
					lexer.next().getText() +    // a
					lexer.next().getText() +    // b
					lexer.next().getText();     // c

			assertEquals(input, ouput);
			assertEquals(EOF.class, lexer.next().getClass());
		} catch(Exception e) {
			fail(e.getClass() + ": " + e.getMessage());
		}
	}

	private static Lexer newLexer(final String input) {
		return new Lexer(new PushbackReader(new StringReader(input), input.length()));
	}
}
