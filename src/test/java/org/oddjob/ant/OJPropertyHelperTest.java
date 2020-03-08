package org.oddjob.ant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.MockPropertyManager;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.values.types.PropertyType;

public class OJPropertyHelperTest extends Assert {

	private static final Logger logger = LoggerFactory.getLogger(AntFileTest.class);

	@Rule public TestName name = new TestName();
	
    @Before
    public void setUp() throws Exception {
		logger.info("-----------------------  " + name.getMethodName() + 
				"  ---------------------");
		logger.info("stdout is " + System.out);
	}
	
	private class OurLookup extends MockBeanRegistry {

		public <T> T lookup(String fullPath, Class<T> type) {
			if ("preferences.fruit".equals(fullPath)) {
				return type.cast("Apples");
			}
			return null;
		}
		
	}
	
	private class MyBuildListener implements BuildListener {
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
			if (event.getPriority() <= Project.MSG_INFO) {
				message = event.getMessage();
			}			
		}	
	}

	private class OurSession extends MockArooaSession {

		@Override
		public BeanRegistry getBeanRegistry() {
			return new OurLookup();
		}
		
		@Override
		public PropertyManager getPropertyManager() {
			return new MockPropertyManager() {
				@Override
				public String lookup(String propertyName) {
					return null;
				}
			};
		}
		
		@Override
		public ArooaTools getTools() {
			return new StandardTools();
		}
	}
	
    @Test
	public void testWithProject() throws Exception {
		MyBuildListener listener = new MyBuildListener();
		
		OurSession session = new OurSession();
		
		OJPropertyHelper test = new OJPropertyHelper(session);
		
		Project project = new Project();
		project.init();
		project.addBuildListener(listener);
		project.addBuildListener(new DefaultLogger());

		PropertyHelper ph = PropertyHelper.getPropertyHelper(project);
		ph.add(test);
		
		AntParser ap = new AntParser(project);

		ap.parse("<tasks><echo message='${preferences.fruit}'/></tasks>");
		
		Target result = (Target) project.getTargets().get(
				AntParser.TARGET_NAME);
		assertNotNull(result);
		
		result.execute();
		
		assertEquals("Apples", listener.message);
		
	}

	
    @Test
	public void testArooaValueWithProject() throws ArooaParseException {

		PropertyType root = new PropertyType();
		PropertyType property = (PropertyType) root.get("snack");
		property.set("fruit", "apple");
		
		
		ArooaDescriptor descriptor = new OddjobDescriptorFactory(
				).createDescriptor(getClass().getClassLoader());
		
		StandardArooaSession session = new StandardArooaSession(
				descriptor);
		
		session.getBeanRegistry().register(
				"our", root);
		
		OJPropertyHelper test = new OJPropertyHelper(session);
		
		Project project = new Project();
		project.init();

		PropertyHelper ph = PropertyHelper.getPropertyHelper(project);
		ph.add(test);
		
		String result = project.getProperty("our.snack.fruit");
		
		assertEquals("apple", result);
	}
}
