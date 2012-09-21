package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmSegment;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SelectSnapMoveAction implements MouseMoveAction {
	
	private OsmData osmData;
	private EditLayer editLayer;
	
	public boolean init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
		return true;
	}
	
	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e) {
		
		//get snapObjects
		List<SnapObject> snapObjects = editLayer.getSnapObjects();
		snapObjects.clear();
		
		//check for hovering over these objects
		SnapObject snapObject;
		List<OsmObject> objectList = osmData.getFeatureList();
		for(OsmObject mapObject:objectList) {
			//make sure edit is enabled for this object
			if(!mapObject.editEnabled()) continue;

			//do the hover check
			if(mapObject instanceof OsmNode) {
				//check for a node hit
				snapObject = SnapNode.testNode((OsmNode)mapObject, mouseMerc, mercRadSq);
				if(snapObject != null) {
					snapObjects.add(snapObject);
				}

				//check for a segment hit
				for(OsmSegment segment:((OsmNode)mapObject).getSegments()) {
					if(!segment.editEnabled()) continue;

					//only do the segments that start with this node, to avoid doing them twice
					if(segment.getNode1() == mapObject) {

							//selection preview - when we are selecting an object
							//select objects if no mouse edit action is active

							//check for a virtual node hit
							snapObject = SnapVirtualNode.testVirtualNodeHit(segment, mouseMerc, mercRadSq);
							if(snapObject != null) {
								snapObjects.add(snapObject);
							}

							//check for way hit
							SnapWay.loadHitWays(segment, mouseMerc, mercRadSq, snapObjects);
					}
				}
			}
		}
		
		//order the snap objects and select the active one
		if(snapObjects.size() > 1) {
			Collections.sort(snapObjects);
		}
		int activeSnapObject = snapObjects.isEmpty() ? -1 : 0;
		editLayer.setActiveSnapObject(activeSnapObject);
	}
}
