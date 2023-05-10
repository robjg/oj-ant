package org.oddjob.ant;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;
import org.oddjob.Oddjob;
import org.oddjob.OddjobBuilder;
import org.oddjob.arooa.utils.QuoteTokenizerFactory;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * This is an ant task which runs Oddjob.
 * <p>
 * This is an example of using it in a build file.
 * <pre>
 * &lt;taskdef name="oddjob" 
 *             classname="org.oddjob.ant.OddjobTask"/&gt;
 *
 * &lt;target name="simple"
 *        description="Run Something simple"/&gt;
 *   &lt;oddjob config="simple/oddjob.xml"/&gt;
 * &lt;/target&gt;
 * </pre>
 *
 * @author Rob Gordon
 */

public class OddjobTask extends Task {
	
	public static final String ODDJOB_HOME_PROPERTY = "oddjob.home";
	
	private File file;
	
	private String name;
	
	private boolean inheritAll;

	private File oddballsDir;
	
	private boolean noOddballs;
	
	private String oddballsPath;
	
	private String args;
	
    /** the properties to pass to the new project */
    private final Vector<Property> properties = new Vector<>();
	
	/**
	 * Execute the job.
	 */	
	public void execute() throws BuildException {

		Project newProject = new Project();
		
        Enumeration<Property> enumeration = properties.elements();
        while (enumeration.hasMoreElements()) {
            Property p = enumeration.nextElement();
            p.setProject(newProject);
            p.execute();
        }

		Hashtable<String, Object> antProperties = getProject().getProperties();
		antProperties.putAll(newProject.getProperties());
		Properties properties = new Properties();
		properties.putAll(antProperties);
		
		OddjobBuilder oddjobBuilder = new OddjobBuilder();
		
		String oddjobHome = (String) antProperties.get(
				ODDJOB_HOME_PROPERTY);

		oddjobBuilder.setOddjobHome(oddjobHome);
		
		File file = this.file;
		if (file == null) {
			file = getProject().resolveFile("oddjob.xml");
		}

		oddjobBuilder.setOddjobFile(file.toString());
		
		oddjobBuilder.setName(name);
		oddjobBuilder.setNoOddballs(noOddballs);
		oddjobBuilder.setOddballsDir(oddballsDir);
		oddjobBuilder.setOddballsPath(oddballsPath);
		
		try {
			Oddjob oddjob = oddjobBuilder.buildOddjob()
					.orElseThrow();

			if (args != null) {
				oddjob.setArgs(new QuoteTokenizerFactory(
						"\\s+", '"', '\\').newTokenizer().parse(args));
			}
			
			oddjob.setProperties(properties);
			
			
			log("Running Oddjob [" + oddjob + "], configuration " +
					file.getCanonicalPath());
			
			oddjob.run();
			
			log("Ran Oddjob [" + oddjob + "], state " +
					oddjob.lastStateEvent().getState());
		} 
		catch (Exception e) {
			throw new BuildException(e);
		}		
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
	
	public boolean isInheritAll() {
		return inheritAll;
	}

	public void setInheritAll(boolean inheritAll) {
		this.inheritAll = inheritAll;
	}

    /**
     * Property to pass to the new project.
     * The property is passed as a 'user property'.
     * @return the created <code>Property</code> object.
     */
    public Property createProperty() {
        Property p = new Property();
        p.setTaskName("property");
        properties.addElement(p);
        return p;
    }

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getOddballsDir() {
		return oddballsDir;
	}

	public void setOddballsDir(File oddballsDir) {
		this.oddballsDir = oddballsDir;
	}

	public boolean isNoOddballs() {
		return noOddballs;
	}

	public void setNoOddballs(boolean noOddballs) {
		this.noOddballs = noOddballs;
	}

	public String getOddballsPath() {
		return oddballsPath;
	}

	public void setOddballsPath(String oddballsPath) {
		this.oddballsPath = oddballsPath;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}
	
}

