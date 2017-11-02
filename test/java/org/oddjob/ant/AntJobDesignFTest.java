package org.oddjob.ant;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.OddjobTestHelper;
import org.xml.sax.SAXException;
import org.xmlunit.matchers.CompareMatcher;

public class AntJobDesignFTest {

	private static final Logger logger = Logger.getLogger(AntJobDesignFTest.class);
	
	@Rule public TestName name = new TestName();

	@Before
    public void setUp() {
		logger.info("========================== " + name.getMethodName() + "===================" );
		logger.info("stdout is " + System.out);
	}

	DesignInstance design;
	
    @Test
	public void testCreate() throws ArooaParseException, SAXException, IOException {
		
		String xml = 
				"<ant name='Test' project='${myproject}' messageLevel='ERR'" +
				"	classPath='x/y/*.jar'>" +
				" <tasks>" +
				"  <xml>" +
				"   <echo message='Hello'/>" +
				"  </xml>" +
				" </tasks>" +
				" <classLoader>" +
				"  <url-class-loader>" +
				"   <files>" +
				"    <file file='mystuff.jar'/>" +
				"   </files>" +
				"  </url-class-loader>" +
				" </classLoader>" +
				" <output>" +
				"  <buffer/>" +
				" </output>" +
				"</ant>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();		
		
		assertEquals(AntJobDesign.class, design.getClass());
		
		AntJob test = (AntJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());

		String expectedTasks = "<echo message='Hello'/>";

		Assert.assertThat(test.getTasks(), CompareMatcher.isSimilarTo(expectedTasks));
		
		assertEquals("URLClassLoader: " + 
				new File("mystuff.jar").getAbsolutePath(), 
				test.getClassLoader().toString());

		assertEquals("x/y/*.jar", test.getClassPath());
		assertEquals("ERR", test.getMessageLevel());
	}

	public static void main(String args[]) throws ArooaParseException, SAXException, IOException {

		AntJobDesignFTest test = new AntJobDesignFTest();
		test.testCreate();

		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}	
}
