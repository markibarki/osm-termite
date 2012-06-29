package intransix.osm.termite.gui.task;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmModel;
import intransix.osm.termite.map.data.OsmParser;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.BlockerDialog;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class MapDataRequestTask extends SwingWorker<OsmData,Object> {
	
	private String url;
	private OsmData osmData;
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
	public OsmData doInBackground() {
		
		try {
			OsmParser osmParser = new OsmParser();
			osmData = osmParser.parse(url);
			success = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		return osmData;
	}
	
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.setVisible(false);
		}
		
		if(success) {
			gui.setMapData(osmData);
		}
		else {
			JOptionPane.showMessageDialog(null,"There was an error: " + errorMsg);
		}	
	}
}
