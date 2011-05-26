package org.oddjob.ant;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;

public class AntParserTest extends TestCase {

	class MyBuildListener implements BuildListener {
		String message;
		
		public void buildStarted(BuildEvent event) {
		}
		public void buildFinished(BuildEvent event) {
		}
		public void targetStarted(BuildEvent event) {
		}
		public void targetFinished(BuildEvent event) {
		}
		public void taskStarted(BuildEvent event) {
		}
		public void taskFinished(BuildEvent event) {
		}
		public void messageLogged(BuildEvent event) {
			message = event.getMessage();
		}	
	}
	
	
	public void testParseTask() throws Exception {
		MyBuildListener listener = new MyBuildListener();
		
		String xml = 
				"<tasks>" +
				"	<echo message='Hello World'/>" +
				"</tasks>";
		
		Project project = new Project();
		project.init();
		project.addBuildListener(listener);
		
		AntParser test = new AntParser(project);
		test.parse(xml);
				
		Target result = (Target) project.getTargets().get(
				AntParser.TARGET_NAME);
		assertNotNull(result);
		
		result.execute();
		
		assertEquals("Hello World", listener.message);
	}
	
	public void testPropertyAndTask() throws Exception {
		MyBuildListener listener = new MyBuildListener();
		
		String xml = 
				"<tasks>" +
				"	<property name='preferences.fruit' value='Apples'/>" +
				"	<echo message='${preferences.fruit}'/>" +
				"</tasks>";
		
		Project project = new Project();
		project.init();
		project.addBuildListener(listener);
		
		AntParser test = new AntParser(project);
		test.parse(xml);
				
		Target result = (Target) project.getTargets().get(
				AntParser.TARGET_NAME);
		assertNotNull(result);
		
		result.execute();
		
		assertEquals("Apples", listener.message);
	}
	
	public void testSharedProject() throws Exception {
		MyBuildListener listener = new MyBuildListener();
		
		String xml = 
				"<tasks>" +
				"	<property name='preferences.fruit' value='Apples'/>" +
				"</tasks>";
		
		Project project = new Project();
		project.init();
		project.addBuildListener(listener);
		
		AntParser test = new AntParser(project);
		test.parse(xml);

		Target result = (Target) project.getTargets().get(
				AntParser.TARGET_NAME);
		assertNotNull(result);
		
		result.execute();
		
		xml = 
			"<tasks>" +
			"	<echo message='${preferences.fruit}'/>" +
			"</tasks>";

		test.parse(xml);
		
		result = (Target) project.getTargets().get(
				AntParser.TARGET_NAME);
		assertNotNull(result);
		
		result.execute();
		
		assertEquals("Apples", listener.message);
	}
	

	public static class FooTask extends Task {
		private String stuff;
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
		public String getStuff() {
			return stuff;
		}
	}
	
	public void testTaskDef() throws Exception {
		
		String xml = 
				"<tasks>" +
				"	<taskdef name='foo' classname='" + FooTask.class.getName() + "'/>" +
				"	<foo stuff='Apples'/>" +
				"</tasks>";
		
		Project project = new Project();
		project.init();
		
		AntParser test = new AntParser(project);
		test.parse(xml);
				
		Target target = (Target) project.getTargets().get(
				AntParser.TARGET_NAME);
		assertNotNull(target);

		target.execute();
		
		Task[] tasks = target.getTasks();
		
		assertEquals(2, tasks.length);
		
		UnknownElement unknownElement= ((UnknownElement) tasks[1]);
		
		unknownElement.maybeConfigure();
		
		FooTask appleTask = (FooTask) unknownElement.getRealThing();
		
		assertEquals("Apples", appleTask.stuff);
	}
	
}
