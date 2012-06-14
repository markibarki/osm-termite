/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmNode;

/**
 *
 * @author sutter
 */
public class UpdatePosition implements EditData<OsmNode> {
	
	private double x;
	private double y;
	
	public UpdatePosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmNode> readInitialData(OsmNode node) throws UnchangedException {
		double initialX = node.getX();
		double initialY = node.getY();
		UpdatePosition undoUpdate = new UpdatePosition(initialX,initialY);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmNode node) throws UnchangedException, Exception {
		//set the property
		node.setPosition(x,y);
		
		//flag as dirty the node, way and level
		TermiteNode termiteNode = (TermiteNode)node.getTermiteObject();
		termiteNode.incrementTermiteVersion();
		//explicitly marks ways as changed
		for(TermiteWay termiteWay:termiteNode.getWays()) {
			termiteWay.incrementTermiteVersion();
		}
		//explicitly mark level as changed
		TermiteLevel level = termiteNode.getLevel();
		if(level != null) {
			level.incrementTermiteVersion();
		}
	}
}
