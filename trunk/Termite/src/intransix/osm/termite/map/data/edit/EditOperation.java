package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class EditOperation {
	
	private OsmData osmData;
	
	public EditOperation(OsmData osmData) {
		this.osmData = osmData;
	}
	
	public OsmData getOsmData() {
		return osmData;
	}
	
	public static void reportError(String actionDesc) {
		JOptionPane.showMessageDialog(null,"There was an unknown error on the action: " + actionDesc);
	}
	
	public static void reportFatalError(String actionDesc, String exceptionMsg) {
		JOptionPane.showMessageDialog(null,"There was a fatal error on the action: " + actionDesc +
				"; " + exceptionMsg + "The application must exit");
		System.exit(-1);
	}
	
	
}
