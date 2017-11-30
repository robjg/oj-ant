package org.oddjob.ant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PropertyHelper;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.runtime.Evaluator;

/**
 * Perform the property replacement. This allows
 * Oddjob properties to be used in Ant.
 *  
 * @author rob
 */
public class OJPropertyHelper implements PropertyHelper.PropertyEvaluator {
	private static final Logger logger = LoggerFactory.getLogger(OJPropertyHelper.class);
	
	private final ArooaSession session;
	
	public OJPropertyHelper(ArooaSession session) {
		this.session = session;
	}
	
	@Override
	public Object evaluate(String property, PropertyHelper propertyHelper) {
		
		Evaluator evaluator = session.getTools().getEvaluator();

		String result = null;
		try {
			result = evaluator.evaluate(property, session, String.class);
		}
		catch (ArooaConversionException e) {
			throw new BuildException("Failed getting property " + property, e);
		}			
		
		logger.debug("Required: [" + property +
				"], returning [" + result + "]");
		
		return result;
	}
}
