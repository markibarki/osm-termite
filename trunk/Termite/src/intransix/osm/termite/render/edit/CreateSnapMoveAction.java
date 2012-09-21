package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmSegment;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sutter
 */
public class CreateSnapMoveAction implements MouseMoveAction {
	
	private OsmData osmData;
	private EditLayer editLayer;
	private List<SnapSegment> workingSnapSegments = new ArrayList<SnapSegment>();
	
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
						//snap preview - when we are in an edit
						//check for segment and extension hit
						//do this when a mouse edit action is active

						SnapSegment snapSegment = SnapSegment.testSegmentHit(segment,
								mouseMerc,mercRadSq);
						if(snapSegment != null) {
							snapObjects.add(snapSegment);
							workingSnapSegments.add(snapSegment);
						}
					}
				}
			}
		}
		
		//check for snapping in the pending snap segments
		SnapSegment ss;
		List<EditSegment> pendingSnapSegments = editLayer.getPendingSnapSegments();
		if(pendingSnapSegments != null) {
			AffineTransform pixelsToMercator= editLayer.getViewRegionManager().getPixelsToMercator();
			Point2D mercPix00 = new Point2D.Double(0,0);
			Point2D mercPix10 = new Point2D.Double(1,0);
			Point2D mercPix01 = new Point2D.Double(0,1);
			pixelsToMercator.transform(mercPix00, mercPix00);
			pixelsToMercator.transform(mercPix10, mercPix10);
			pixelsToMercator.transform(mercPix01, mercPix01);
		
			for(EditSegment es:pendingSnapSegments) {
				//check for horizontal snap
				ss = SnapSegment.getHorOrVertSnapSegment(es,mouseMerc,mercRadSq,mercPix00,mercPix10,mercPix01);
				if(ss != null) {
					snapObjects.add(ss);
					workingSnapSegments.add(ss);
				}
				else {
					//only check perpicular if it is not already a horizontal or vertical snap
					//check for perps from both ends
					ss = SnapSegment.getPerpSegment(es,mouseMerc,mercRadSq);
					if(ss != null) {
						snapObjects.add(ss);
						workingSnapSegments.add(ss);
					}
				}
			}
		}
		
		//check for intersections
		workingSnapSegments.clear();
		SnapIntersection.loadIntersections(workingSnapSegments, mouseMerc, mercRadSq, snapObjects);
		workingSnapSegments.clear();
		
		//order the snap objects and select the active one
		if(snapObjects.size() > 1) {
			Collections.sort(snapObjects);
		}
		int activeSnapObject = snapObjects.isEmpty() ? -1 : 0;
		editLayer.setActiveSnapObject(activeSnapObject);
	}
}
