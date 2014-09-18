package custom;

import org.apache.tools.ant.Task;

public class ATask extends Task {
	public void execute() {
		log("ATask Worked.");
		getProject().setProperty("test.result", "Worked");
	}
}
