package org.oddjob.ant;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLTestCase;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;

public class AntJobXMLTest extends XMLTestCase {

	private static final Logger logger = Logger.getLogger(
			AntJobXMLTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.info("---------------  " + getName() + " ---------------");
	}
	
	static final String LS = System.getProperty("line.separator");
		
	public void testXML() throws Exception {
		
		// Note the _ant because 'ant' masks required ant properties!!!
		
		String config = 
			"<oddjob>" +
			" <job>" +
			"  <ant id='ant_'>" +
			"   <tasks>" +
			"	 <xml>" +
			"<tasks>" +
			" <echo message='apple'/>" + 
			"</tasks>"+
			"    </xml>" +
			"   </tasks>" +
			"  </ant>" +
			" </job>" +
			"</oddjob>";
	
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", config));
		
		oddjob.run();
		
		String result = new OddjobLookup(oddjob).lookup(
				"ant_.tasks", String.class);
		
		String expected = 
			"<tasks>" + LS + 
			"    <echo message=\"apple\"/>" + LS +
			"</tasks>";
		
		assertXMLEqual(expected, result);
	}
		
}
