/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.sequence;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.dialog.ConfirmDialog;

/**
 *
 * @author sutter
 */
public class ClearDataSequence {
	
	private MapDataManager mapDataManager;

	public void clearData(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
		
		Runnable cancelCallback = new Runnable() {
			public void run() {
				clearCanceled();
			}
		};
		
		Runnable doClearCallback = new Runnable() {
			public void run() {
				doClear();
			}
		};
		
		ConfirmDialog.show("Do you want to clear all the current data?", doClearCallback, cancelCallback);
	}
	
	public void clearCanceled() {
	}
	
	public void doClear() {
		mapDataManager.clearData();
	}
}
