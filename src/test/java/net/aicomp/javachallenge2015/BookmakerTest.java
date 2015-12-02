package net.aicomp.javachallenge2015;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

import net.aicomp.javachallenge2015.log.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BookmakerTest {

	private ByteArrayOutputStream _sysout;
	private ByteArrayOutputStream _syserr;
	private PrintStream _out;
	private PrintStream _err;

	@Before
	public void setUp() {
		_sysout = new ByteArrayOutputStream();
		_out = System.out;
		System.setOut(new PrintStream(new BufferedOutputStream(_sysout)));
		_syserr = new ByteArrayOutputStream();
		_err = System.err;
		System.setErr(new PrintStream(new BufferedOutputStream(_syserr)));
	}

	@After
	public void tearDown() {
		System.setOut(_out);
		System.setErr(_err);
	}

	@Test
	public void testNew() {
		main(new String[] { "-a", "\"java SampleAIL -s 0\"", "-a", "\"java SampleAIL -s 20\"", "-a",
				"\"java SampleAIL -s 40\"", "-a", "\"java SampleAIL -s 60\"", "-s", "0", "-t", "3" });
		System.out.flush();
		String expected = getFileContents(new File("fixture/sample_log_new.txt"));
		String actual = _sysout.toString();
		assertEquals(normalize(expected), normalize(actual));
	}

	@Test
	public void testTimeout() {
		main(new String[] { "-a", "\"java TimeoutAI\"", "-a", "\"java TimeoutAI\"", "-a", "\"java TimeoutAI\"", "-a",
				"\"java TimeoutAI\"", "-s", "0", "-t", "30" });
		System.err.flush();
		String actual = _syserr.toString();
		assertEquals(
				actual.length()
						- actual.replace("Terminated the thread because time was exceeded.",
								"Terminated the thread because time was exceeded").length(), 4);
	}

	@Test
	public void testRandomWalk() {
		main(new String[] { "-a", "\"java SampleAI -s 0\"", "-a", "\"java SampleAI -s 0\"", "-a",
				"\"java SampleAI -s 0\"", "-a", "\"java SampleAI -s 0\"", "-s", "0", "-t", "30" });
		System.out.flush();
		String expected = getFileContents(new File("fixture/sample_log_seed0.txt"));
		String actual = _sysout.toString();
		assertEquals(normalize(expected), normalize(actual));
	}

	@Test
	public void testWithTestAI() {
		main(new String[] { "-a", "\"java TestAI -c L R N U L L\"", "-a", "\"java TestAI -c N U A\"", "-a",
				"\"java TestAI -c L A N U L L\"", "-a", "\"java SampleAI -s 0\"", "-s", "0", "-t", "30" });
		System.out.flush();
		String expected = getFileContents(new File("fixture/testWithTestAI.txt"));
		String actual = _sysout.toString();
		assertEquals(normalize(expected), normalize(actual));
	}

	private void main(String[] args) {
		Options options = Bookmaker.buildOptions();

		try {
			Logger.initialize();
			CommandLineParser parser = new DefaultParser();
			CommandLine line = parser.parse(options, args);
			Bookmaker.start(line);
		} catch (ParseException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(-1);
		} finally {
			Logger.outputLog("Game Finished!");
			Logger.close();
		}
	}

	private String getFileContents(File file) {
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		}
		StringBuilder builder = new StringBuilder();
		while (sc.hasNextLine()) {
			builder.append(sc.nextLine());
			builder.append("\n");
		}
		if (sc != null) {
			sc.close();
		}
		return builder.toString();
	}

	private String normalize(String text) {
		return text.replace("\\r\\n", "\\n").replace("\r\n", "\n");
	}

}
