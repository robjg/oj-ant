package org.oddjob.ant;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.oddjob.tools.OurDirs;

import junit.framework.TestCase;

public class OddjobTaskTest extends TestCase {

	private static final Logger logger = Logger.getLogger(OddjobTaskTest.class);

	protected void setUp() throws Exception {
		logger.info("-----------------------  " + getName() + 
				"  ---------------------");
		logger.info("stdout is " + System.out);
	}
	
	public void testRunSimpleOddjob() {
		
		OurDirs dirs = new OurDirs();
		
        Project project = new Project();
        project.init();
        
        OddjobTask test = new OddjobTask();
        test.setProject(project);
        test.setFile(dirs.relative(
        		"test/files/oddjob-hello.xml"));
        
        test.execute();        
	}
}
