package org.oddjob.ant;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.io.BufferType;
import org.oddjob.tools.OurDirs;

public class AntFileTest extends TestCase {

	private static final Logger logger = Logger.getLogger(AntFileTest.class);

	protected void setUp() throws Exception {
		logger.info("-----------------------  " + getName() + 
				"  ---------------------");
		logger.info("stdout is " + System.out);
	}
	
	String EOL = System.getProperty("line.separator");
	
	public void testAntFile() throws IOException {
		
		OurDirs dirs = new OurDirs();
		
		String xml = 
			"<tasks>" +
			" <ant antfile='" + dirs.base() + "/test/files/ant-file.xml' " +
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
			"     [echo] " + new File(dirs.base() + "/test/files/ant-file.xml").getCanonicalPath() + EOL;
					
		assertEquals(expected, result);
	}
	
	
}
