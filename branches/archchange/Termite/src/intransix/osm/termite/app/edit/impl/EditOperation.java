package intransix.osm.termite.app.edit.impl;

import intransix.osm.termite.app.mapdata.MapDataManager;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class EditOperation {
	
	private MapDataManager mapDataManager;
	
	public EditOperation(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
	}
	
	public MapDataManager getMapDataManager() {
		return  mapDataManager;
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
