package intransix.osm.termite.gui.task;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmChangeSet;
import intransix.osm.termite.map.data.OsmParser;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.BlockerDialog;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class CommitTask extends SwingWorker<Object,Object>{
	
	private String message;
	private OsmData osmData;
	private TermiteGui gui;
	private JDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public CommitTask(TermiteGui gui, OsmData osmData, String message) {
		this.gui = gui;
		this.osmData = osmData;
		this.message = message;
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(gui,this,"Loading map data...",false);
			blocker.setVisible(true);
		}
	}
	
	@Override
	public Object doInBackground() {
		
		try {
			OsmChangeSet changeSet = new OsmChangeSet();
			changeSet.setMessage(message);
			osmData.loadChangeSet(changeSet);
			
			success = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		return "";
	}
	
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.setVisible(false);
		}
		
		if(success) {
			//for now just get rid of the data
			gui.setMapData(null);
		}
		else {
			JOptionPane.showMessageDialog(null,"There was an error: " + errorMsg);
		}	
	}
}
