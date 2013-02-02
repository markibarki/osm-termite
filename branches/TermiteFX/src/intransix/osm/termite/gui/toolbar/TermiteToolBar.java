package intransix.osm.termite.gui.toolbar;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author sutter
 */
public class TermiteToolBar extends HBox {
	private ToolBar modeToolBar;
	private ToolBar submodeToolBar;
	
	public TermiteToolBar() {
		modeToolBar = new ToolBar();
		this.getChildren().add(modeToolBar);

		modeToolBar.getItems().add(new Button("Test"));	
	}
	
}
