package intransix.osm.termite.gui.dialog;

import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 *
 * @author sutter
 */
public class MessageDialog {
	/** This brings up a dialog with the given message. The call returns immediately but the UI will be blocked
	 * until the user presses ok. */
	public static void show(Stage parent, String msg) {
		DialogCallback callback = new DialogCallback() {
			@Override
			public boolean handle(TermiteDialog dialog) {
				return true;
			}
		};
		show(parent,msg,callback);
	}
	
	/** This method brings up a dialog with the given message. The call returns immediately but the UI will be blocked.
	 * It calls the given callback when the user presses the OK button. */
	public static void show(Stage parent, String msg, DialogCallback callback) {
		TermiteDialog dialog = new TermiteDialog(parent);
		Label label = new Label(msg);
		dialog.init(label, callback, null);
		dialog.show();
	}
}
