package intransix.osm.termite.gui.task;

import intransix.osm.termite.map.model.TermiteData;
import intransix.osm.termite.map.osm.OsmData;
import intransix.osm.termite.map.osm.OsmModel;
import intransix.osm.termite.map.osm.OsmParser;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.BlockerDialog;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class MapDataRequestTask extends SwingWorker<TermiteData,Object> {
	
	private String url;
	private TermiteData termiteData;
	private TermiteGui gui;
	private JDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public MapDataRequestTask(TermiteGui gui, double minLat, double minLon, 
			double maxLat, double maxLon) {
		this.gui = gui;
		this.url = OsmModel.getBBoxRequestUrl(minLat, minLon, maxLat, maxLon);
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(gui,this,"Loading map data...",false);
			blocker.setVisible(true);
		}
	}
	
	@Override
	public TermiteData doInBackground() {
		
		try {
			OsmParser osmParser = new OsmParser();
			OsmData osmData = osmParser.parse(url);
			termiteData = new TermiteData();
			termiteData.loadData(osmData);
			success = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		return termiteData;
	}
	
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.setVisible(false);
		}
		
		if(success) {
			gui.setEditData(termiteData);
		}
		else {
			JOptionPane.showMessageDialog(null,"There was an error: " + errorMsg);
		}	
	}
}
