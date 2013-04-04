/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.sequence;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.TermiteFXGui;
import intransix.osm.termite.gui.dialog.ConfirmDialog;
import intransix.osm.termite.gui.dialog.DialogCallback;
import intransix.osm.termite.gui.dialog.TermiteDialog;

/**
 *
 * @author sutter
 */
public class ClearDataSequence {
	
	private MapDataManager mapDataManager;

	public void clearData(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
		
		DialogCallback cancelCallback = new DialogCallback() {
			@Override
			public boolean handle(TermiteDialog dialog) {
				clearCanceled();
				return true;
			}
		};
		
		DialogCallback doClearCallback = new DialogCallback() {
			@Override
			public boolean handle(TermiteDialog dialog) {
				doClear();
				return true;
			}
		};
		
		ConfirmDialog.show(TermiteFXGui.getStage(),"Do you want to clear all the current data?", doClearCallback, cancelCallback);
	}
	
	public void clearCanceled() {
	}
	
	public void doClear() {
		mapDataManager.clearData();
	}
}
