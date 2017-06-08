import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import test.analysis.DepthFirstAdapter;
import test.lexer.Lexer;
import test.lexer.LexerException;
import test.node.*;
import test.parser.Parser;
import test.parser.ParserException;

import java.io.*;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Parser tests.
 *
 * @author Kevin Krumwiede
 */
public class ParserTest {

	@Rule
	public final Stopwatch stopwatch = new Stopwatch() {
		@Override
		protected void succeeded(final long nanos, final Description description) {
			System.out.println(description + " ran in " + nanos / 1000000 + " ms");
		}
	};

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
			e.printStackTrace();
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


	@Test
	public void testPerformance() {
		final int testDataSize = 10000000;

		final Parser parser = new Parser(new Lexer(new PushbackReader(new BufferedReader(testInput(testDataSize)), 6)));
		int tokensReturned = 0;
		int charsReturned = 0;
		while(true) {
			try {
				final Start start = parser.parse();
				if(start.getPAlt() != null) {
					++tokensReturned;
					charsReturned += start.getPAlt().getText().length();
				}
				if(start.getEOF() != null) {
					break;
				}
			} catch(Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		System.out.println("parsed " + tokensReturned + " tokens totaling " + charsReturned + " characters out of " + testDataSize + " in stream");
	}

	private static Parser newParser(final String input) {
		return new Parser(new Lexer(new PushbackReader(new StringReader(input), Math.max(1, input.length()))));
	}

	private static Reader testInput(final int length) {
		return new Reader() {
			private int mReturned = 0;
			private final int mLimit = length;
			private final Random mRand = new Random(13013);
			private boolean mClosed;

			@Override
			public int read(final char[] buffer, final int off, final int len) throws IOException {
				if(mClosed) {
					throw new IOException("closed");
				}
				if(mReturned == mLimit) {
					return -1;
				}
				final int limit = Math.min(len, mLimit - mReturned);
				int returned = 0;
				while(returned < limit) {
					final int token = mRand.nextInt(30);
					if(token < 6) {
						for(int i = 0; i <= token && returned < limit; ++i) {
							buffer[off + returned++] = (char) ('a' + i);
						}
					}
					else {
						buffer[off + returned++] = (char) (mRand.nextInt(26) + 'a');
					}
				}
				mReturned += returned;
				return limit;
			}

			@Override
			public void close() throws IOException {
				mClosed = true;
			}
		};
	}
}
