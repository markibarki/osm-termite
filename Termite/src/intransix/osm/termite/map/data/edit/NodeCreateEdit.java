package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import javax.swing.JOptionPane;
import java.util.List;

/**
 *
 * @author sutter
 */
public class NodeCreateEdit extends EditOperation {
	
	public NodeCreateEdit(OsmData osmData) {
		super(osmData);
	}
	
	public OsmNode nodeToolClicked(EditDestPoint destPoint, FeatureInfo featureInfo, 
			OsmRelation currentLevel) {
System.out.println("Create a node");

		EditAction action = new EditAction(getOsmData(),"Create Node");

if(destPoint.snapNode != null) {
	JOptionPane.showMessageDialog(null,"Creating a node feature on an existing node not currently supported");
	return null;
}

		try {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(destPoint.point.getX(),destPoint.point.getY());

			//get the properties
			List<PropertyPair> properties = OsmModel.featureInfoMap.getFeatureProperties(featureInfo);
			for(PropertyPair pp:properties) {
				nodeSrc.addProperty(pp.key,pp.value);
			}
			
			EditInstruction instr = new CreateInstruction(nodeSrc,getOsmData());
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
}
