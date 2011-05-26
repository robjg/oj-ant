package org.oddjob.ant;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PropertyHelper;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.registry.BeanDirectory;

/**
 * Perform the property replacement. This allows
 * Oddjob properties to be used in Ant.
 *  
 * @author rob
 */
public class OJPropertyHelper implements PropertyHelper.PropertyEvaluator {
	private static final Logger logger = Logger.getLogger(OJPropertyHelper.class);
	
	private final ArooaSession session;
	
	public OJPropertyHelper(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public Object evaluate(String property, PropertyHelper propertyHelper) {
		
		String result = session.getPropertyManager().lookup(property);

		if (result == null) {
			
			BeanDirectory lookup = session.getBeanRegistry();

			try {
				result = lookup.lookup(property, String.class);
			}
			catch (ArooaConversionException e) {
				throw new BuildException("Failed getting property " + property, e);
			}			
		}
		
		logger.debug("Required: [" + property +
				"], returning [" + result + "]");
		
		return result;
	}
}
