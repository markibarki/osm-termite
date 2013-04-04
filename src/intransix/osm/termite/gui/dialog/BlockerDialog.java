package intransix.osm.termite.gui.dialog;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 *
 * @author sutter
 */
public class BlockerDialog extends TermiteDialog {
	
	private Task task;
	
	public BlockerDialog(Stage parent, Task task, String msg, boolean cancelable) {
		super(parent);
		
		DialogCallback cancelHandler = null;
		if(cancelable) {
			cancelHandler = new DialogCallback() {
				@Override
				public boolean handle(TermiteDialog dialog) {
					BlockerDialog.this.task.cancel();
					return true;
				}
			};
		}
		this.task = task;
		
		//construct dialog
		Label label = new Label(msg);
		this.init(label, null, cancelHandler);
	}
}
