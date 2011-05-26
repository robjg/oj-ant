package org.oddjob.ant;


import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.oddjob.Oddjob;

/**
 * This is an ant task which runs Oddjob.
 * <p>
 * This is an example of using it in a build file.
 * <pre>
 * &lt;taskdef name="oddjob" 
 *             classname="org.oddjob.ant.OddjobTask"/&gt;
 *
 * &lt;target name="simple"
 *        description="Run somehting simple"/&gt;
 *   &lt;oddjob config="simple/oddjob.xml"/&gt;
 * &lt/target&gt;
 *
 * @author Rob Gordon
 */

public class OddjobTask extends Task {
	
	private File file;
	
	private String name;
	
	/**
	 * Set the config file name.
	 * 
	 * @param configFile The config file name.
	 */	
	public void setConfig(File configFile) {
		this.file = configFile;
	}

	/**
	 * Get the config file name.
	 * 
	 * @return The config file name.
	 */
	public File getConfig() {
		return file;
	}
		
	/**
	 * Set the name.
	 * 
	 * @param name The name.
	 */	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Execute the job.
	 */	
	public void execute() throws BuildException {
		
		File file = this.file;
		if (file == null) {
			file = getProject().resolveFile("oddjob.xml");
		}

		Oddjob oddjob = new Oddjob();	
		oddjob.setFile(file);
		
		if (name == null) {
			oddjob.setName(name);
		}
		
		try {
			log("Running Oddjob [" + oddjob.toString() + "], configuration " +
					file.getCanonicalPath());
		} catch (IOException e) {
			throw new BuildException(e);
		}
		
		oddjob.run();
		
		log("Ran Oddjob [" + oddjob.toString() + "], state " + 
				oddjob.lastJobStateEvent().getJobState());

	}
	
}

