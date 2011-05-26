import org.apache.tools.ant.Task;

public class ATask extends Task {
	public void execute() {
		getProject().setProperty("test.result", "Worked");
	}
}
