package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author sutter
 */
public class WayNodeEdit extends EditOperation {
	
	public WayNodeEdit(OsmData osmData) {
		super(osmData);
	}
	
	
	public OsmWay wayToolClicked(OsmWay activeWay, boolean isEnd, 
			EditDestPoint destPoint, FeatureInfo featureInfo, OsmRelation currentLevel) {
		
System.out.println("Add a node to a way");

		if(activeWay == null) {
			return createWay(destPoint,featureInfo, currentLevel);
		}
		else {
			int insertIndex;
			if(isEnd) insertIndex = activeWay.getNodes().size();
			else insertIndex = 0;
			addNodeToWay(activeWay,destPoint,insertIndex,currentLevel);
			return activeWay;
		}
	}
	
	public OsmNode nodeInserted(OsmSegment segment, EditDestPoint dest, OsmRelation currentLevel) {
		
System.out.println("Insert node into way");

		//create action
		EditAction action = new EditAction(getOsmData(),"Insert Node into Way");
			
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
				OsmNode node = getOsmData().getOsmNode(nodeId);
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
	
	
	private OsmWay createWay(EditDestPoint dest, FeatureInfo featureInfo, OsmRelation currentLevel) {
		EditAction action = new EditAction(getOsmData(),"Create Way");
		
		Long startNodeId = createOrUseExistingNode(action,dest,currentLevel);
		
		OsmWaySrc waySrc = new OsmWaySrc();
		if(startNodeId != null) {
			List<Long> nodeIds = waySrc.getNodeIds();
			nodeIds.add(startNodeId);
		}
		
		//get the properties
		List<PropertyPair> properties = OsmModel.featureInfoMap.getFeatureProperties(featureInfo);
		for(PropertyPair pp:properties) {
			waySrc.addProperty(pp.key,pp.value);
		}
			
		EditInstruction instr = new CreateInstruction(waySrc,getOsmData());
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			if(success) {
				long id = waySrc.getId();
				return getOsmData().getOsmWay(id);
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
		EditAction action = new EditAction(getOsmData(),"Add node to way");
		
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
	
	private void insertNodeIntoWay(EditAction action, OsmWay way, long nodeId, int index) {
		UpdateInsertNode uin = new UpdateInsertNode(nodeId,index);
		EditInstruction instr = new UpdateInstruction(way,uin);
		action.addInstruction(instr);
	}
	
	private Long createOrUseExistingNode(EditAction action, EditDestPoint dest, OsmRelation currentLevel) {
		
		Long startNodeId = null;
		if(dest.snapNode != null) {
			startNodeId = dest.snapNode.getId();
		}
		else if(dest.point != null) {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(dest.point.getX(),dest.point.getY());
			EditInstruction instr = new CreateInstruction(nodeSrc,getOsmData());
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
	
}
