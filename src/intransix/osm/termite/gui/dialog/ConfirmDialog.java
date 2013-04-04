package intransix.osm.termite.gui.dialog;

import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 *
 * @author sutter
 */
public class ConfirmDialog  {
	public static void show(Stage parent, String msg, final DialogCallback okCallback, final DialogCallback cancelCallback) {
		TermiteDialog dialog = new TermiteDialog(parent);
		Label label = new Label(msg);
		dialog.init(label, okCallback, cancelCallback);
		dialog.show();
	}
}
