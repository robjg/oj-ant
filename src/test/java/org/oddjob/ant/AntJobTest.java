/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.ant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.BufferType;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.tools.CompileJob;
import org.oddjob.tools.FragmentHelper;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.OurDirs;
import org.oddjob.util.ClassLoaderDiagnostics;

/**
 * Tests for AntJob.
 */
public class AntJobTest extends Assert {

	private static final Logger logger = LoggerFactory.getLogger(AntJobTest.class);
	
	private static final String LS = System.lineSeparator();
	
	@Rule public TestName name = new TestName();

    @Before
    public void setUp() {

		
		logger.info("------------------  " + name.getMethodName() + "-----------------");
		logger.info("stdout is " + System.out);
		
		results = new HashMap<>();
	}
	
	public static class Result implements Runnable {
		String result;
		public void setResult(String result) {
			this.result = result;
		}
		public String getResult() {
			return result;
		}
		// only implement run so component is configured
		// after ant task is run.
		public void run() {
			
		}
	}
	
    @Test
	public void testJob() throws ArooaConversionException {
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/ant/AntEchoAndCapture.xml",
				getClass().getClassLoader()));
		oddjob.setArgs(new String[] { "greeting" });
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		String s = new OddjobLookup(oddjob).lookup("result", String.class);
		
		assertEquals("     [echo] greeting" + LS, s);
		
		// check version.
		String version = new OddjobLookup(oddjob).lookup("an-ant.version", 
				String.class);
		
		assertTrue(version.startsWith("Apache Ant"));
		
		logger.info("stdout is " + System.out);
		
		oddjob.destroy();
	}

    @Test
	public void testSettingPropertiesInAnt() throws ArooaParseException {
		
		FragmentHelper helper = new FragmentHelper();
		
		AntJob test = (AntJob) helper.createComponentFromResource(
				"org/oddjob/ant/AntSettingPropertiesInAnt.xml");
		
		BufferType buffer = new BufferType();
		buffer.configured();
		test.setOutput(buffer.toOutputStream());
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
				
		assertEquals("[echo] Test", buffer.getText().trim());
	}
	
	
	
	// use a map so each test can set a different entry.
	private static Map<String, Object> results;
	
	public static class ResultTask extends Task {
		String key;
		String result;
		
		public void setKey(String key) {
			this.key = key;
		}
		
		public void setResult(String result) {
			this.result = result;
		}
		
		public void execute() throws BuildException {
			results.put(key, result);
		}
	}
		
    @Test
	public void testUsingOddjobProperty() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/ant/AntUsingOddjobProperties.xml",
				getClass().getClassLoader()));
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		assertEquals("Apples", results.get("one"));
		assertEquals("Apples", results.get("two"));
		
	}
	
	public static class ExceptionTask extends Task {
		public void execute() throws BuildException {
			throw new RuntimeException("Ahhhhh!");
		}
	}
	
    @Test
	public void testException() throws ArooaConversionException {
		
		String config = 
			"<oddjob>" +
			" <job>" +
			"  <ant id='foo' exception='true'>" +
			"    <tasks>" +
			"     <xml>" +
			"      <tasks>" +
			"    <taskdef name='exception' classname='" + ExceptionTask.class.getName() + "'/>" +
			"	 <exception/>" +
			"      </tasks>" +
			"     </xml>" +
			"    </tasks>" +
			"  </ant>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
		oj.run();
		
		assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());
		
		class L implements StateListener {
			private String em;
			public void jobStateChange(StateEvent event) {
				em = event.getException().getCause().getMessage();
			}
		}
		L l = new L();

		Stateful stateful = new OddjobLookup(oj).lookup(
				"foo", Stateful.class);
		
		stateful.addStateListener(l);
		
		assertEquals("Ahhhhh!", l.em);
	}
	
    @Test
	public void testSharedProject() {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/ant/AntSharingProjects.xml",
				getClass().getClassLoader()));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		assertEquals("Apples", results.get("three"));
		assertEquals("Apples", results.get("four"));
		
	}
	
    @Test
	public void testChangingProperty() {
		String config = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <variables id='v'>" +
			"     <fruit>" +
			"      <value value='Apples'/>" +
			"     </fruit>" +
			"    </variables>" +
			"    <ant id='defs'>" +
			"     <tasks>" +
			"      <xml>" +
			"       <tasks>" +
			"     <taskdef name='result' classname='" + ResultTask.class.getName() + "'/>" +
			"     <property name='our.fruit' value='${v.fruit}'/>" +
			"     <property name='v.fruit' value='Pears'/>" +
			"        </tasks>" +
			"       </xml>" +
			"      </tasks>" +
			"    </ant>" +
			"    <set>" +
			"     <values>" +
			"      <value key='v.fruit' value='Pears'/>" +
			"     </values>" +
			"    </set>" +
			"    <echo>${v.fruit}</echo>" +
			"    <ant project='${defs.project}'>" +
			"     <tasks>" +
			"      <xml>" +
			"       <tasks>" +
			"     <property name='our.fruit' value='${v.fruit}'/>" +
			"	  <result key='five' result='${our.fruit}'/>" +
			"	  <result key='six' result='${v.fruit}'/>" +
			"       </tasks>" +
			"      </xml>" +
			"     </tasks>" +
			"    </ant>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		assertEquals("Apples", results.get("five"));
		assertEquals("Pears", results.get("six"));
		
	}

	public static void compileATask(OurDirs dirs) {
		
		CompileJob compile = new CompileJob();
		compile.setFiles(new File[] {
				dirs.relative("test/ant/custom/ATask.java")
		});

		compile.run();
		
		assertEquals(0, compile.getResult());
		
	}
	
	
	/**
	 * Test setting a classpath for a taskdef.
	 * 
	 * @throws Exception
	 */
    @Test
	public void testClassPath() throws Exception {

		OurDirs dirs = new OurDirs();
		
		compileATask(dirs);
		
		ClassLoaderDiagnostics diagnostics = 
			new ClassLoaderDiagnostics();
		
		diagnostics.setClassLoader(this.getClass().getClassLoader());
		diagnostics.setClassName("com.sun.tools.javac.Main");
		
		diagnostics.run();
		
		
		Oddjob oddjob = new Oddjob();
		
		File config = dirs.relative("test/files/AntTaskDefWithClasspath.xml");
		
		oddjob.setFile(config);
		oddjob.setArgs(new String[] { dirs.base().toString() });
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

		AntJob antJob = new OddjobLookup(oddjob).lookup("myant", AntJob.class);
		
		assertEquals("Worked", antJob.getProject().getProperty("test.result"));
	
		oddjob.destroy();
	}
	
	public static class Exists extends Task {
		File file;
		public void setFile(File file) {
			this.file = file;
		}
		public void execute() throws BuildException {
			results.put("basedirtest", file.exists());
		}
	}
	
	
    @Test
	public void testBaseDir() {
		
		String config = 
			"<oddjob id='this'>" +
			" <job>" +
			"  <ant baseDir='${this.args[0]}/test/ant'>" +
			"   <tasks>" +
			"    <xml>" +
			"     <tasks>" +
			"    <taskdef name='result' classname='" + Exists.class.getName() + "'/>" +
			"	 <result file='custom/ATask.java'/>" +
			"     </tasks>" +
			"    </xml>" +
			"   </tasks>" +
			"  </ant>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
		oj.setArgs(new String[] { new OurDirs().base().toString() });
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		assertEquals(Boolean.TRUE, results.get("basedirtest"));
	}

    @Test
	public void testSerialize() throws IOException, ClassNotFoundException {
	
		StandardArooaSession session = new StandardArooaSession();
		
		AntJob test = new AntJob();
		test.setArooaSession(session);

		OddjobTestHelper.register(test, session, null);
		
		test.setTasks("<tasks><echo message='Hello World'/></tasks>");
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		AntJob copy = OddjobTestHelper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());		
	}
	
	public static class HangingTask extends Task {
		
		public void execute() throws BuildException {
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
				throw new BuildException(e);
			}
		}
	}
	
    @Test
	public void testStop() throws InterruptedException, FailedToStopException {
		String config = 
			"<oddjob>" +
			" <job>" +
			"  <ant>" +
			"   <tasks>" +
			"    <xml>" +
			"     <tasks>" +
			"    <taskdef name='hang' classname='" + HangingTask.class.getName() + "'/>" +
			"	 <hang/>" +
			"     </tasks>" +
			"    </xml>" +
			"   </tasks>" +
			"  </ant>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
		
		Thread t = new Thread(oj);
		t.start();
		
		Thread.sleep(2000);
		
		oj.stop();

		t.join();
		
		assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());	
	}
	
    @Test
	public void testReset() {
	
		
		AntJob test = new AntJob();
		test.setArooaSession(new StandardArooaSession());
		test.setTasks("<tasks><echo message='ok'/></tasks>");
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		assertNotNull(test.getProject());
		
		test.hardReset();
		
		assertNull(test.getProject());
	}
	
    @Test
	public void testWorkingWithFileExample() {

		OurDirs dirs = new OurDirs();
		
		File workDir = dirs.relative("work/files");
		workDir.mkdirs();

		Properties properties = new Properties();
		properties.setProperty("work.dir", workDir.getPath());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/ant/AntWorkingWithFiles.xml",
				getClass().getClassLoader()));
		oddjob.setProperties(properties);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
}
