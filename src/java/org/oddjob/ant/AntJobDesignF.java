/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.ant;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;


/**
 *
 */
public class AntJobDesignF implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new AntJobDesign(element, parentContext);
	}
		
}

class AntJobDesign extends BaseDC {
	
	private final SimpleTextAttribute project;

	private final SimpleTextAttribute messageLevel;
	
	private final SimpleDesignProperty output;
	
	private final SimpleDesignProperty tasks;
	
	private final FileAttribute baseDir;

	private final SimpleTextAttribute exception;

	private final SimpleTextAttribute classPath;
	
	private final SimpleDesignProperty classLoader;
	
	public AntJobDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		project = new SimpleTextAttribute("project", this);
		
		baseDir = new FileAttribute("baseDir", this);

		tasks = new SimpleDesignProperty("tasks", this);

		messageLevel = new SimpleTextAttribute("messageLevel", this);
		
		output = new SimpleDesignProperty("output", this);

		exception = new SimpleTextAttribute("exception", this);
		
		classPath = new SimpleTextAttribute("classPath", this);
		
		classLoader = new SimpleDesignProperty("classLoader", this);
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, project, 
				baseDir, tasks, messageLevel, 
				output, exception, classPath, classLoader };
	}
	
	public Form detail() {
		return 
			new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(new BorderedGroup("Properties")
					.add(project.view().setTitle("Project"))
					.add(baseDir.view().setTitle("Base Dir"))
					.add(tasks.view().setTitle("Tasks"))
					.add(messageLevel.view().setTitle("Message Level"))
					.add(output.view().setTitle("Output"))
					.add(exception.view().setTitle("Exception"))
					.add(classPath.view().setTitle("ClassPath"))
					.add(classLoader.view().setTitle("Class Loader"))
				);
	}
	
}
