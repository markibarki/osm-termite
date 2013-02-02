package intransix.osm.termite.app.edit.impl;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.dataset.OsmNodeSrc;
import intransix.osm.termite.util.PropertyPair;
import intransix.osm.termite.app.mapdata.instruction.EditInstruction;
import intransix.osm.termite.app.mapdata.instruction.CreateInstruction;
import intransix.osm.termite.app.mapdata.instruction.EditAction;
import intransix.osm.termite.app.mapdata.instruction.UpdateInsertMember;
import intransix.osm.termite.app.mapdata.instruction.UpdateInstruction;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import javax.swing.JOptionPane;
import java.util.List;

/**
 *
 * @author sutter
 */
public class NodeCreateEdit extends EditOperation {
	
	public NodeCreateEdit(MapDataManager mapDataManager) {
		super(mapDataManager);
	}
	
	public OsmNode nodeToolClicked(EditDestPoint destPoint, FeatureInfo featureInfo, 
			OsmRelation currentLevel) {
System.out.println("Create a node");

		EditAction action = new EditAction(getMapDataManager(),"Create Node");

if(destPoint.snapNode != null) {
	JOptionPane.showMessageDialog(null,"Creating a node feature on an existing node not currently supported");
	return null;
}

		try {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(destPoint.point.getX(),destPoint.point.getY());

			//get the properties
//@TODO clean this line up
			FeatureTypeManager featureTypeManager = getMapDataManager().getRenderLayer().getFeatureTypeManager();
			List<PropertyPair> properties = featureTypeManager.getFeatureProperties(featureInfo);
			for(PropertyPair pp:properties) {
				nodeSrc.putProperty(pp.key,pp.value);
			}
			
			EditInstruction instr = new CreateInstruction(nodeSrc,getMapDataManager());
			action.addInstruction(instr);
			long nodeId = nodeSrc.getId();
			
			if(currentLevel != null) {
				//place node on the current level
				UpdateInsertMember uim = new UpdateInsertMember(nodeId,OsmModel.TYPE_NODE,OsmModel.ROLE_NODE);
				instr = new UpdateInstruction(currentLevel,uim);
				action.addInstruction(instr);
			}
			
			boolean success = action.doAction();
			if(success) {
				OsmData osmData = getMapDataManager().getOsmData();
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
}
