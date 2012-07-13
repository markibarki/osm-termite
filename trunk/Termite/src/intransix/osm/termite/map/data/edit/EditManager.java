package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.JOptionPane;

//clean this up...
import intransix.osm.termite.render.edit.EditObject;

/**
 *
 * @author sutter
 */
public class EditManager {
	
	private OsmData osmData;
	
	public EditManager(OsmData osmData) {
		this.osmData = osmData;
	}
	
	public OsmNode nodeToolClicked(EditDestPoint destPoint, FeatureInfo featureInfo, 
			OsmRelation currentLevel) {
System.out.println("Create a node");

		EditAction action = new EditAction(osmData,"Create Node");

if(destPoint.snapNode != null) {
	JOptionPane.showMessageDialog(null,"Creating a node on another node not currently supported");
	return null;
}

		try {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(destPoint.point.getX(),destPoint.point.getY());
//need to add properties!!!
			EditInstruction instr = new CreateInstruction(nodeSrc,osmData);
			action.addInstruction(instr);
			long nodeId = nodeSrc.getId();
			
			if(currentLevel != null) {
				//place node on the current level
				UpdateInsertMember uim = new UpdateInsertMember(nodeId,OsmModel.TYPE_NODE,OsmModel.ROLE_FEATURE);
				instr = new UpdateInstruction(currentLevel,uim);
				action.addInstruction(instr);
			}
			
			boolean success = action.doAction();
			if(success) {

				OsmNode node = osmData.getOsmNode(nodeId);
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
	
	public OsmWay wayToolClicked(OsmWay activeWay, int insertIndex, 
			EditDestPoint destPoint, FeatureInfo featureInfo, OsmRelation currentLevel) {
		
System.out.println("Add a node to a way");

		if(activeWay == null) {
			return createWay(destPoint,featureInfo, currentLevel);
		}
		else {
			addNodeToWay(activeWay,destPoint,insertIndex,currentLevel);
			return activeWay;
		}
	}
	
	public boolean selectionMoved(List<Object> selection, EditDestPoint start,
			EditDestPoint dest) {
	
//for now disallow this
if((start.snapNode != null)&&(dest.snapNode != null)) {
	JOptionPane.showMessageDialog(null,"Creating a node on another node not currently supported");
	return false;
}
		
System.out.println("Move the selection");

		HashSet<OsmNode> nodeSet = new HashSet<OsmNode>();
		for(Object selectObject:selection) {
			if(selectObject instanceof OsmNode) {
				nodeSet.add((OsmNode)selectObject);
			}
			else if(selectObject instanceof OsmWay) {
				for(OsmNode node:((OsmWay)selectObject).getNodes()) {
					nodeSet.add(node);
				}
			}
		}

		double dx = dest.point.getX() - start.point.getX();
		double dy = dest.point.getY() - start.point.getY();

		return moveNodes(nodeSet,dx,dy);
	}
	
	public OsmNode nodeInserted(OsmSegment segment, EditDestPoint dest, OsmRelation currentLevel) {
		
System.out.println("Insert node into way");

		//create action
		EditAction action = new EditAction(osmData,"Insert Node into Way");
			
		try {
			//create node, if needed
			Long nodeId = createOrUseExistingNode(action,dest,currentLevel);

			//add node to each way containing this segment
			int insertIndex = 1;
			boolean found = false;
			for(OsmWay way:segment.getOsmWays()) {
				for(OsmSegmentWrapper osw:way.getSegments()) {
					if(segment == osw.segment) {
						found = true;
						break;
					}
					insertIndex++;
				}
				if(!found) continue;

				insertNodeIntoWay(action,way,nodeId,insertIndex);
			}

			//execute action
			boolean success = action.doAction();
			if(success) {
				OsmNode node = osmData.getOsmNode(nodeId);
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
	
	//==========================
	// Private Methods
	//==========================
	
	private OsmWay createWay(EditDestPoint dest, FeatureInfo featureInfo, OsmRelation currentLevel) {
		EditAction action = new EditAction(osmData,"Create Way");
		
		Long startNodeId = createOrUseExistingNode(action,dest,currentLevel);
		
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
	
	private boolean addNodeToWay(OsmWay way, EditDestPoint dest, int insertIndex, OsmRelation currentLevel) {
		EditAction action = new EditAction(osmData,"Add node to way");
		
		Long addNodeId = createOrUseExistingNode(action,dest,currentLevel);
		insertNodeIntoWay(action,way,addNodeId,insertIndex);
		
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
	
	private boolean moveNodes(Collection<OsmNode> nodes, double dx, double dy) {
		EditAction action = new EditAction(osmData,"Create Node");

		try {
			for(OsmNode node:nodes) {
				UpdatePosition up = new UpdatePosition(node.getPoint().getX() + dx,
						node.getPoint().getY() + dy);
				EditInstruction instr = new UpdateInstruction(node,up);
				action.addInstruction(instr);
			}
			
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
	
	private Long createOrUseExistingNode(EditAction action, EditDestPoint dest, OsmRelation currentLevel) {
		
		Long startNodeId = null;
		if(dest.snapNode != null) {
			startNodeId = dest.snapNode.getId();
		}
		else if(dest.point != null) {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(dest.point.getX(),dest.point.getY());
			EditInstruction instr = new CreateInstruction(nodeSrc,osmData);
			action.addInstruction(instr);
			startNodeId = nodeSrc.getId();
			
			if(currentLevel != null) {
				//place node on the current level
				UpdateInsertMember uim = new UpdateInsertMember(startNodeId,OsmModel.TYPE_NODE,OsmModel.ROLE_FEATURE);
				instr = new UpdateInstruction(currentLevel,uim);
				action.addInstruction(instr);
			}
		}
		
		return startNodeId;
	}
	
	private void insertNodeIntoWay(EditAction action, OsmWay way, long nodeId, int index) {
		UpdateInsertNode uin = new UpdateInsertNode(nodeId,index);
		EditInstruction instr = new UpdateInstruction(way,uin);
		action.addInstruction(instr);
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
