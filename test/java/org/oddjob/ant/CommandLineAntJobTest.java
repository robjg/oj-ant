package org.oddjob.ant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.jobs.ExecJob;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobSrc;
import org.oddjob.tools.OurDirs;

public class CommandLineAntJobTest extends Assert {
	
	private static final Logger logger = LoggerFactory.getLogger(
			CommandLineAntJobTest.class);
	
	final static String RUN_JAR = "run-oddjob.jar";
	
	final static String EOL = System.getProperty("line.separator");
	
	@Rule public TestName name = new TestName();

	/** The oddjob project dir */
	File oddjobHome;
		
    @Before
    public void setUp() throws Exception {
		logger.info("---------------- " + name.getMethodName() + " -----------------");
		logger.info("stdout is " + System.out);
		
		this.oddjobHome = new OddjobSrc().oddjobSrcBase();		
		File runJar = new File(this.oddjobHome, RUN_JAR);

		
		assertTrue(runJar.exists());		
	}
	
	String relative(String fileName) {
		try {
			return new File(oddjobHome, fileName).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    @Test
	public void testTaskDefWithClasspath() throws InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/AntTaskDefWithClasspath.xml");

		File antOddball = dirs.base();
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + relative(RUN_JAR) + 
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
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
	public void testTaskDefWithAntLib() throws InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/AntTaskDefWithAntLib.xml");

		File antOddball = dirs.base();
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + relative(RUN_JAR) + 
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
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
	public void testAntLibWithAntClassLoader() throws InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/OddjobCallsAntWithAntLib.xml");

		File antOddball = dirs.base();
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + relative(RUN_JAR) + 
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
			exec.run();
		}
		
		console.dump();
		
		assertEquals(0, exec.getExitValue());
		
		String[] lines = console.getLines();
		
		assertEquals("test:", lines[1].trim());
		assertEquals("Ant Job Failed as expected!", lines[lines.length - 1].trim());
		
		exec.destroy();
	}
	
    @Test
	public void testOddjobLaunchAntLib() throws InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		AntJobTest.compileATask(dirs);
		
		File oddjobFile = dirs.relative("test/files/OddjobLaunchWithAntLib.xml");

		File antOddball = dirs.base();
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar " + relative(RUN_JAR) + 
				" -nb -op " + antOddball + " -f " + oddjobFile
				+ " -l " + relative("test/launch/logback.xml"));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
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
