package org.oddjob.ant;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLTestCase;
import org.oddjob.Helper;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.xml.sax.SAXException;

public class AntJobDesignFTest extends XMLTestCase {

	private static final Logger logger = Logger.getLogger(AntJobDesignFTest.class);
	
	public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
	public void testCreate() throws ArooaParseException, SAXException, IOException {
		
		String xml = 
				"<ant name='Test' project='${myproject}' messageLevel='ERR'>" +
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
		
		AntJob test = (AntJob) Helper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());

		String expectedTasks = "<echo message='Hello'/>";

		assertXMLEqual(expectedTasks, test.getTasks());
		
		assertEquals("URLClassLoader: mystuff.jar", test.getClassLoader().toString());
		assertEquals("ERR", test.getMessageLevel());
	}

	public static void main(String args[]) throws ArooaParseException, SAXException, IOException {

		AntJobDesignFTest test = new AntJobDesignFTest();
		test.testCreate();

		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}	
}
