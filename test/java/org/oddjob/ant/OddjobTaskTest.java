package org.oddjob.ant;

import org.apache.tools.ant.Project;
import org.oddjob.tools.OurDirs;

import junit.framework.TestCase;

public class OddjobTaskTest extends TestCase {

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
