package org.oddjob.ant;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.oddjob.OddjobSrc;
import org.oddjob.OurDirs;
import org.oddjob.jobs.ExecJob;
import org.oddjob.tools.ConsoleCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Tag("IntegrationTest")
public class CommandLineAntJobTest extends Assert {
	
	private static final Logger logger = LoggerFactory.getLogger(
			CommandLineAntJobTest.class);
	
	private final static String RUN_JAR = "run-oddjob.jar";

	/** The oddjob app dir */
	private File oddjobApp;

	/** The this project dir */
	private File ourHome;

	private File runJar;

    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception {
		logger.info("---------------- " + testInfo.getDisplayName() + " -----------------");
		logger.info("stdout is " + System.out);

		this.ourHome = new OurDirs().base();

		this.oddjobApp = new OddjobSrc().oddjobApp();
		this.runJar = new File(this.oddjobApp, RUN_JAR);

		assertTrue(runJar.exists());		
	}
	
	private String relative(String fileName) {
		try {
			return new File(ourHome, fileName).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    @Test
	public void testTaskDefWithClasspath() {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/AntTaskDefWithClasspath.xml");

		File antOddball = dirs.relative("target/oddball");
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + runJar +
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close ignored = console.capture(exec.consoleLog())) {
			
			exec.run();
		}
		
		console.dump();
		
		assertEquals(0, exec.getExitValue());
		
		String[] lines = console.getLines();
		
		assertEquals("[atask] ATask Worked.", lines[0].trim());
		
		assertEquals(1, lines.length);

		exec.destroy();
	}

    @Test
	public void testTaskDefWithAntLib() {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/AntTaskDefWithAntLib.xml");

		File antOddball = dirs.relative("target/oddball");
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + runJar +
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close ignored = console.capture(exec.consoleLog())) {
			
			exec.run();
		}
		
		console.dump();
		
		assertEquals(0, exec.getExitValue());
		
		String[] lines = console.getLines();
		
		assertEquals("[oj:atask] ATask Worked.", lines[0].trim());
		
		assertEquals(1, lines.length);

		exec.destroy();
	}
	
    @Test
	public void testAntLibWithAntClassLoader() {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/OddjobCallsAntWithAntLib.xml");

		File antOddball = dirs.relative("target/oddball");
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + runJar +
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close ignored = console.capture(exec.consoleLog())) {
			
			exec.run();
		}
		
		console.dump();
		
		assertEquals(0, exec.getExitValue());
		
		String[] lines = console.getLines();
		
		assertEquals("test:", lines[1].trim());
		assertEquals("Ant Job Failed as expected!", lines[lines.length - 1].trim());
		
		exec.destroy();
	}

	@Disabled("Launch Job needs fixing first")
    @Test
	public void testOddjobLaunchAntLib() {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/OddjobLaunchWithAntLib.xml");

		File antOddball = dirs.relative("target/oddball");
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + runJar +
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close ignored = console.capture(exec.consoleLog())) {
			
			exec.run();
		}
		
		console.dump();
		
		assertEquals(0, exec.getExitValue());
		
		String[] lines = console.getLines();
		
		assertEquals("test:", lines[2].trim());
		assertEquals("BUILD SUCCESSFUL", lines[lines.length - 2].trim());
		
		exec.destroy();
	}
}
