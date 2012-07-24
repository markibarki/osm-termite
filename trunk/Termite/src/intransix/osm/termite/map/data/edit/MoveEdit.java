package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class MoveEdit extends EditOperation {
	
	public MoveEdit(OsmData osmData) {
		super(osmData);
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
	
	private boolean moveNodes(Collection<OsmNode> nodes, double dx, double dy) {
		EditAction action = new EditAction(getOsmData(),"Create Node");

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
}
