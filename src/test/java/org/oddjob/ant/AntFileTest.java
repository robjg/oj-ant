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
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.io.BufferType;

public class AntFileTest extends Assert {

	private static final Logger logger = LoggerFactory.getLogger(AntFileTest.class);

	@Rule public TestName name = new TestName();

	@Before
    public void setUp() {
		logger.info("-----------------------  " + name.getMethodName() + 
				"  ---------------------");
		logger.info("stdout is " + System.out);
	}
	
	private final static String EOL = System.lineSeparator();

    @Test
	public void testAntFile() throws IOException {
		
		File antFile = new File(
				getClass().getResource("ant-file.xml").getFile());
		
		String xml = 
			"<tasks>" +
			" <ant antfile='" + antFile.getPath() + "' " +
			"      inheritAll='false' target='test'/>" +
			"</tasks>";
		
		AntJob test = new AntJob();
		test.setArooaSession(new StandardArooaSession());
		test.setTasks(xml);
		
		BufferType buffer = new BufferType();
		buffer.configured();
		
		test.setOutput(buffer.toOutputStream());
		test.run();
		
		String result = buffer.getText();
		
		String expected = EOL +
			"test:" + EOL +
			"     [echo] " + antFile.getCanonicalPath() + EOL;
					
		assertEquals(expected, result);
	}
	
    @Test
	public void testOddjobAntWithAntTaskInheritsProperties() throws ArooaPropertyException, ArooaConversionException {
		
		File file = new File(
				getClass().getResource("OddjobAntAntTaskInheritiedProperties.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.run();
		
		String result = new OddjobLookup(oddjob).lookup(
				"results", String.class);
		
		String expected = EOL + 
				"test-param:" + EOL +
				"     [echo] Apples" + EOL;
						
			assertEquals(expected, result);
	}
}
