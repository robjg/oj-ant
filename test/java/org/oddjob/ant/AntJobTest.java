/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.ant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.tools.CompileJob;
import org.oddjob.util.ClassLoaderDiagnostics;

/**
 * Tests for AntJob.
 */
public class AntJobTest extends TestCase {
	
	static final String LS = System.getProperty("line.separator");
	
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
	
	public void testJob() throws ArooaConversionException {
				
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/ant/AntEchoAndCapture.xml",
				getClass().getClassLoader()));
		oj.setArgs(new String[] { "greeting" });
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		String s = new OddjobLookup(oj).lookup("result", String.class);
		
		assertEquals("     [echo] greeting" + LS, s);
		
		// check version.
		String version = new OddjobLookup(oj).lookup("an-ant.version", 
				String.class);
		
		assertTrue(version.startsWith("Apache Ant"));
		
		oj.destroy();
	}
	
	// use a map so each test can set a different entry
	// as it looks like eclipse runs tests in parallel.
	static final Map<String, Object> results = 
		new HashMap<String, Object>();
	
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
		
	public void testUsingOddjobProperty() {
		
		String config =
			"<oddjob id='this'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <variables id='v'>" +
			"     <fruit>" +
			"      <value value='Apples'/>" +
			"     </fruit>" +
			"    </variables>" +
			"    <ant>" +
			"     <tasks>" +
			"      <xml>" +
			"       <tasks>" +
			"     <taskdef name='result' classname='" + ResultTask.class.getName() + "'/>" +
			"      <property name='our.fruit' value='${v.fruit}'/>" +
			"      <property name='v.fruit' value='Pears'/>" +
			"	   <result key='one' result='${our.fruit}'/>" +
			"	   <result key='two' result='${v.fruit}'/>" +
			"        </tasks>" +
			"       </xml>" +
			"      </tasks>" +
			"	 </ant>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		assertEquals("Apples", results.get("one"));
		assertEquals("Apples", results.get("two"));
		
	}
	
	public static class ExceptionTask extends Task {
		public void execute() throws BuildException {
			throw new RuntimeException("Ahhhhh!");
		}
	}
	
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
			String em;
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
	
	public void testSharedProject() {
		
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
			"    <ant project='${defs.project}'>" +
			"     <tasks>" +
			"      <xml>" +
			"       <tasks>" +
			"     <property name='our.fruit' value='Pears'/>" +
			"	  <result key='three' result='${our.fruit}'/>" +
			"	  <result key='four' result='${v.fruit}'/>" +
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
		
		assertEquals("Apples", results.get("three"));
		assertEquals("Apples", results.get("four"));
		
	}
	
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
			"    <echo text='${v.fruit}'/>" +
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

	/**
	 * Test setting a classpath for a taskdef.
	 * 
	 * To prove the test works we must have a task which is 
	 * outside of the project classpath. To this end we first
	 * use the javac task - and this provides a nice example
	 * of the nightmares of class loading because when running
	 * from eclipse the compiler lives outside the standard
	 * classpath and the javac task uses the default class loader
	 * only to try and load the compile. To get round this we 
	 * use an ArooaClassLoader that loads tasks on a 
	 * classpath that includes the compiler but loads Project
	 * etc on the default classpath. This is because AntJob loads
	 * project using the default classloader, and we are using
	 * a parent first ArooaClassLoader, so javac loads it's own
	 * project which is in a different ClassLoader namespace!!
	 * 
	 * Other options to avoid this would be to load Project, Target
	 * etc on a the Context ClassLoader - but this would require
	 * a lot of reflection or to create a separate oj.ant.jar which
	 * could be loaded on a separate classloader.
	 * 
	 * @throws Exception
	 */
	public void testClassPath() throws Exception {

		OurDirs dirs = new OurDirs();
		
		CompileJob compile = new CompileJob();
		compile.setFiles(new File[] {
				dirs.relative("test/ant/ATask.java")
		});

		compile.run();
		
		assertEquals(0, compile.getResult());
		
		ClassLoaderDiagnostics diagnostics = 
			new ClassLoaderDiagnostics();
		
		diagnostics.setClassLoader(this.getClass().getClassLoader());
		diagnostics.setClassName("com.sun.tools.javac.Main");
		
		diagnostics.run();
		
		String config =
			"<oddjob id='this'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <ant id='a' baseDir='${this.args[0]}'>" +
			"     <tasks>" +
			"      <xml>" +
			"       <tasks>" +
//			"     <javac srcdir='test/ant'" +
//			"		     destdir='test/ant'>" +
//			"      <classpath>" +
//			"        <pathelement location='lib/ant.jar'/>" +
//			"      </classpath>" +
//			"     </javac>" +
			"     <path id='classpath'>" + 
			"      <pathelement location='test/ant'/>" +
			"     </path>" +
			"     <taskdef name='result' classname='ATask'" +
			"              classpathref='classpath'/>" +
			"     <result/>" +
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
		oj.setArgs(new String[] { dirs.base().toString() });
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

		AntJob aj = new OddjobLookup(oj).lookup("a", AntJob.class);
		
		assertEquals("Worked", aj.getProject().getProperty("test.result"));
		
	}
	
	public static class Exists extends Task {
		File file;
		public void setFile(File file) {
			this.file = file;
		}
		public void execute() throws BuildException {
			results.put("basedirtest", new Boolean(file.exists()));
		}
	}
	
	
	public void testBaseDir() {
		String config = 
			"<oddjob id='this'>" +
			" <job>" +
			"  <ant baseDir='${this.args[0]}/test/ant'>" +
			"   <tasks>" +
			"    <xml>" +
			"     <tasks>" +
			"    <taskdef name='result' classname='" + Exists.class.getName() + "'/>" +
			"	 <result file='ATask.java'/>" +
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
		
		assertEquals(new Boolean(true), results.get("basedirtest"));
	}

	public void testSerialize() throws IOException, ClassNotFoundException {
	
		StandardArooaSession session = new StandardArooaSession();
		
		AntJob test = new AntJob();
		test.setArooaSession(session);

		Helper.register(test, session, null);
		
		test.setTasks("<tasks><echo message='Hello World'/></tasks>");
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		AntJob copy = Helper.copy(test);
		
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
}
