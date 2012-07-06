package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class EditManager {
	
	private OsmData osmData;
	
	public EditManager(OsmData osmData) {
		this.osmData = osmData;
	}
	
	public OsmNode nodeToolClicked(EditDestPoint destPoint, FeatureInfo featureInfo) {
System.out.println("Create a node");

		EditAction action = new EditAction(osmData,"Create Node");

if((destPoint.snapNode != null)&&(destPoint.snapNode2 == null)) {
	JOptionPane.showMessageDialog(null,"Creating a node on another node not currently supported");
	return null;
}

		try {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(destPoint.point.getX(),destPoint.point.getY());
//need to add properties!!!
			EditInstruction instr = new CreateInstruction(nodeSrc,osmData);
			action.addInstruction(instr);
			
			boolean success = action.doAction();
			if(success) {
				long id = nodeSrc.getId();
				OsmNode node = osmData.getOsmNode(id);
				return node;
			}
			else {
				reportError(action.getDesc());
				return null;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return null;
		}
	}
	
	public OsmWay wayToolClicked(OsmWay activeWay, boolean isEnd, 
			EditDestPoint destPoint, FeatureInfo featureInfo) {
		
System.out.println("Add a node to a way");
		
		if(activeWay == null) {
			return createWay(destPoint,featureInfo);
		}
		else {
			addNodeToWay(activeWay,destPoint,isEnd);
			return activeWay;
		}
	}
	
	public boolean selectionMoved(List<OsmObject> selection, EditDestPoint startPoint,
			EditDestPoint destPoint) {
		
System.out.println("Move the selection");

		if(selection.size() == 1) {
			OsmObject obj = selection.get(0);
			if(obj instanceof OsmNode) {
				return moveNode((OsmNode)obj,destPoint);
			}
			else {
				JOptionPane.showMessageDialog(null,"For now only a single node can be moved.");
				return false;
			}
		}
		else { 
			JOptionPane.showMessageDialog(null,"For now only a single node can be moved.");
			return false;
		}
	}
	
	private OsmWay createWay(EditDestPoint dest, FeatureInfo featureInfo) {
		EditAction action = new EditAction(osmData,"Create Way");
		
		Long startNodeId = null;
		if((dest.snapNode != null)&&(dest.snapNode2 == null)) {
			startNodeId = dest.snapNode.getId();
		}
		else if(dest.point != null) {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(dest.point.getX(),dest.point.getY());
			EditInstruction instr = new CreateInstruction(nodeSrc,osmData);
			action.addInstruction(instr);
			startNodeId = nodeSrc.getId();
		}
		
		OsmWaySrc waySrc = new OsmWaySrc();
//need to add properties!!!
		if(startNodeId != null) {
			List<Long> nodeIds = waySrc.getNodeIds();
			nodeIds.add(startNodeId);
		}
		EditInstruction instr = new CreateInstruction(waySrc,osmData);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			if(success) {
				long id = waySrc.getId();
				return osmData.getOsmWay(id);
			}
			else {
				reportError(action.getDesc());
				return null;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return null;
		}
	}
	
	private boolean addNodeToWay(OsmWay way, EditDestPoint dest, boolean isEnd) {
		EditAction action = new EditAction(osmData,"Add node to way");
		
		long addNodeId;
		if((dest.snapNode != null)&&(dest.snapNode2 == null)) {
			addNodeId = dest.snapNode.getId();
		}
		else if(dest.point != null) {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(dest.point.getX(),dest.point.getY());
			EditInstruction instr = new CreateInstruction(nodeSrc,osmData);
			action.addInstruction(instr);
			addNodeId = nodeSrc.getId();
		}
		else {
			JOptionPane.showMessageDialog(null,"Error - the poitn to add to the way was not found");
			return false;
		}
		
		
		UpdateInsertNode uin;
		if(isEnd) {
			uin = new UpdateInsertNode(addNodeId);
		}
		else {
			uin = new UpdateInsertNode(addNodeId,0);
		}
		EditInstruction instr = new UpdateInstruction(way,uin);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			if(!success) reportError(action.getDesc());
			return success;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return false;
		}
	}
	
	private boolean moveNode(OsmNode node, EditDestPoint dest) {
		EditAction action = new EditAction(osmData,"Create Node");

if((dest.snapNode != null)&&(dest.snapNode2 == null)) {
	JOptionPane.showMessageDialog(null,"Creating a node on another node not currently supported");
	return false;
}

		try {
			UpdatePosition up = new UpdatePosition(dest.point.getX(),dest.point.getY());
			EditInstruction instr = new UpdateInstruction(node,up);
			action.addInstruction(instr);
			
			boolean success = action.doAction();
			if(success) {
				return true;
			}
			else {
				reportError(action.getDesc());
				return false;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return false;
		}
	}
	
	private void reportError(String actionDesc) {
		JOptionPane.showMessageDialog(null,"There was an unknown error on the action: " + actionDesc);
	}
	
	private void reportFatalError(String actionDesc, String exceptionMsg) {
		JOptionPane.showMessageDialog(null,"There was a fatal error on the action: " + actionDesc +
				"; " + exceptionMsg + "The application must exit");
		System.exit(-1);
	}

}
