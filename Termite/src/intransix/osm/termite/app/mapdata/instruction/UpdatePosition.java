package intransix.osm.termite.app.mapdata.instruction;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmNode;

/**
 * This class updates the location of a node. 
 * 
 * @author sutter
 */
public class UpdatePosition extends EditData<OsmNode> {
	
	//========================
	// Properties
	//========================
	
	private double x;
	private double y;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor
	 * 
	 * @param x		The target x position, in mercator coordinates
	 * @param y		The target y position, in mercator coordinates
	 */
	public UpdatePosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<OsmNode> readInitialData(OsmData osmData, OsmNode node) throws UnchangedException {
		double initialX = node.getPoint().getX();
		double initialY = node.getPoint().getY();
		UpdatePosition undoUpdate = new UpdatePosition(initialX,initialY);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmData osmData, OsmNode node, int editNumber) throws UnchangedException, Exception {
		//set the property
		node.setPosition(x,y);
		
		node.setDataVersion(editNumber);
		node.setContainingObjectDataVersion(editNumber);
	}
}
