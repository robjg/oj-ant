package org.oddjob.ant;

import java.util.Properties;

import org.apache.tools.ant.Main;

/**
 * Override Ant's main to stop using System.exit.
 * 
 * @author rob
 *
 */
public class AntMainOverride extends Main {

	private volatile int antExitCode;
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.tools.ant.Main#exit(int)
	 */
    protected void exit(int exitCode) {
		this.antExitCode = exitCode;
    }
    
	public int getAntExitCode() {
		return antExitCode;
	}
	
	/**
	 * @see Main#start(String[], Properties, ClassLoader)
	 * 
	 * @param args
	 * @param additionalUserProperties
	 * @param coreLoader
	 */
	public static void start(String[] args,
			Properties additionalUserProperties, ClassLoader coreLoader) {
		AntMainOverride m = new AntMainOverride();
		m.startAnt(args, additionalUserProperties, coreLoader);
		if (m.getAntExitCode() != 0) {
			throw new RuntimeException("Ant Build Failed, exit code " + 
					m.getAntExitCode());
		}
	}

	/**
	 * New main entry point.
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        start(args, null, null);
    }
}
