package org.oddjob.ant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tools.ant.Project;
import org.oddjob.OurDirs;

import org.junit.Assert;

public class OddjobTaskTest extends Assert {

	private static final Logger logger = LoggerFactory.getLogger(OddjobTaskTest.class);

	@Rule public TestName name = new TestName();

	@Before
    public void setUp() throws Exception {
		logger.info("-----------------------  " + name.getMethodName() + 
				"  ---------------------");
		logger.info("stdout is " + System.out);
	}
	
    @Test
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
