package org.oddjob.ant;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public class AntParser extends ProjectHelper2 {

	public static final String TASKS_CONFIG_ROOT_ELEMENT = "tasks";
	
	static final String TARGET_NAME = "ojtarget";
	
	private final Project project;

	public AntParser(Project project) {
		this.project = project;
	}
	
	public void parse(String xml) throws SAXException {
		
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		
		InputSource inputSource = new InputSource(is);
		
		parse(inputSource);
	}
	
	public void parse(InputSource source) 
	throws BuildException {
		
		// ensures baseDir is set.
		project.getBaseDir();
		
        AntXMLContext context = new AntXMLContext(project);
        context.setCurrentTargets(new HashMap<String, Target>());        

        project.addReference("ant.parsing.context", context);
        project.addReference("ant.targets", context.getTargets());
		
        RootHandler handler = new RootHandler(context, 
        		new AntHandler() {

        	public AntHandler onStartChild(String uri, String name, String qname,
        		                                   Attributes attrs,
        		                                   AntXMLContext context)
        		        throws SAXParseException {

        		if (!TASKS_CONFIG_ROOT_ELEMENT.equals(qname)) {
        				throw new RuntimeException(
        						"Ant configuration should be enclosed in a " + 
        						TASKS_CONFIG_ROOT_ELEMENT + " element."); 
        			}
        		
        			return new TasksHandler();
        		}
        });
        
        parse(project, source, handler);
        
	}
	
	
    /**
     * Parses the project file, configuring the project as it goes.
     *
     * @param project the current project
     * @param source  the xml source
     * @param handler the root handler to use (contains the current context)
     * @exception BuildException if the configuration is invalid or cannot
     *                           be read
     */
    private void parse(Project project, InputSource source, RootHandler handler)
            throws BuildException {

        
        try {
            /**
             * SAX 2 style parser used to parse the given file.
             */
            XMLReader parser = JAXPUtils.getNamespaceXMLReader();

            project.log("parsing buildconfig " + source.getSystemId(), Project.MSG_VERBOSE);

            DefaultHandler hb = handler;

            parser.setContentHandler(hb);
            parser.setEntityResolver(hb);
            parser.setErrorHandler(hb);
            parser.setDTDHandler(hb);
            parser.parse(source);
        } catch (SAXParseException exc) {
            Location location = new Location(exc.getSystemId(),
                exc.getLineNumber(), exc.getColumnNumber());

            Throwable t = exc.getException();
            if (t instanceof BuildException) {
                BuildException be = (BuildException) t;
                if (be.getLocation() == Location.UNKNOWN_LOCATION) {
                    be.setLocation(location);
                }
                throw be;
            } else if (t == null) {
                t = exc;
            }

            throw new BuildException(exc.getMessage(), t, location);
        } catch (SAXException exc) {
            Throwable t = exc.getException();
            if (t instanceof BuildException) {
                throw (BuildException) t;
            } else if (t == null) {
                t = exc;
            }
            throw new BuildException(exc.getMessage(), t);
        } catch (FileNotFoundException exc) {
            throw new BuildException(exc);
        } catch (UnsupportedEncodingException exc) {
              throw new BuildException("Encoding of project file "
                                       + source.getSystemId() + " is invalid.",
                                       exc);
        } catch (IOException exc) {
            throw new BuildException("Error reading project file "
                                     + source.getSystemId() + ": " + exc.getMessage(),
                                     exc);
        }
    }

    /**
     * Handler for the dummy "tasks" document element.
     */
    public static class TasksHandler extends AntHandler {

        /**
         * Handle the "tasks" document element.
         *
         * @param uri The namespace URI for this element.
         * @param tag Name of the element which caused this handler
         *            to be created. Should not be <code>null</code>.
         *            Ignored in this implementation.
         * @param qname The qualified name for this element.
         * @param attrs Attributes of the element which caused this
         *              handler to be created. Must not be <code>null</code>.
         * @param context The current context.
         *
         * @exception SAXParseException if an unexpected attribute is encountered.
         */
		public void onStartElement(String uri, String tag, String qname,
                                   Attributes attrs,
                                   AntXMLContext context)
            throws SAXParseException {

            Project project = context.getProject();
            Target target = new Target();
            target.setProject(project);
            target.setLocation(new Location(context.getLocator()));
            context.addTarget(target);


            target.setName(TARGET_NAME);
            context.getCurrentTargets().put(TARGET_NAME, target);
            project.addOrReplaceTarget(TARGET_NAME, target);

        }

        /**
         * Handles the start of an element within a target.
         *
         * @param uri The namespace URI for this element.
         * @param name The name of the element being started.
         *            Will not be <code>null</code>.
         * @param qname The qualified name for this element.
         * @param attrs Attributes of the element being started.
         *              Will not be <code>null</code>.
         * @param context The current context.
         * @return an element handler.
         *
         * @exception SAXParseException if an error occurs when initialising
         *                              the appropriate child handler
         */
        public AntHandler onStartChild(String uri, String name, String qname,
                                       Attributes attrs,
                                       AntXMLContext context)
            throws SAXParseException {
            return new ElementHandler();
        }

        /**
         * Handle the end of the project, sets the current target of the
         * context to be the implicit target.
         *
         * @param uri The namespace URI of the element.
         * @param tag The name of the element.
         * @param context The current context.
         */
        public void onEndElement(String uri, String tag, AntXMLContext context) {
            context.setCurrentTarget(context.getImplicitTarget());
        }
    }
}
