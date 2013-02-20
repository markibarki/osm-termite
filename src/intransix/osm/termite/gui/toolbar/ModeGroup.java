package intransix.osm.termite.gui.toolbar;

import intransix.osm.termite.gui.mode.EditorMode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 *
 * @author sutter
 */
public abstract class ModeGroup extends ToggleGroup implements ChangeListener<Toggle> {
	
	private Toggle currentToggle = null;
	
	public ModeGroup() {
		this.selectedToggleProperty().addListener(this);
	}
	
	public void changed(ObservableValue<? extends Toggle> ov,
			Toggle oldToggle, Toggle newToggle) {

		//dont allow turning off a mode by toggling it
		if((newToggle == null)&&(oldToggle != null)) {
			//this is a toggle off of an active button. We don't want this.
			oldToggle.setSelected(true);
			return;
		}
		else if(newToggle == currentToggle) {
			//this is the reselect event, do not pass on
			return;
		}
		else {
			//this is a new selection
			currentToggle = newToggle;
			onSelect(newToggle);
		}
	}
	
	public abstract void onSelect(Toggle toggle);
}
