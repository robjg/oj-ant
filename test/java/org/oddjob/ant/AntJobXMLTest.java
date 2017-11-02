package org.oddjob.ant;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.xmlunit.matchers.CompareMatcher;

public class AntJobXMLTest {

	private static final Logger logger = Logger.getLogger(
			AntJobXMLTest.class);
	
	@Rule public TestName name = new TestName();

	@Before
    public void setUp() throws Exception {
		logger.info("---------------  " + name.getMethodName() + " ---------------");
		logger.info("stdout is " + System.out);
	}
	
	static final String LS = System.getProperty("line.separator");
		
    @Test
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
		
		Assert.assertThat(result, CompareMatcher.isSimilarTo(expected));
	}
		
}
