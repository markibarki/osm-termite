package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.MouseMoveAction;
import intransix.osm.termite.app.edit.snapobject.SnapWay;
import intransix.osm.termite.app.edit.snapobject.SnapObject;
import intransix.osm.termite.app.edit.snapobject.SnapVirtualNode;
import intransix.osm.termite.app.edit.snapobject.SnapNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.map.data.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SelectSnapMoveAction implements MouseMoveAction {
	
	private EditManager editManager;
	
	public SelectSnapMoveAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	@Override
	public boolean init() {
		return true;
	}
	
	@Override
	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e) {
		
		//get snapObjects
		List<SnapObject> snapObjects = editManager.getSnapObjects();
		snapObjects.clear();
		
		//check for hovering over these objects
		OsmData osmData = editManager.getOsmData();
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
							snapObject = testVirtualNodeHit(segment, mouseMerc, mercRadSq);
							if(snapObject != null) {
								snapObjects.add(snapObject);
							}

							//check for way hit
							loadHitWays(segment, mouseMerc, mercRadSq, snapObjects);
					}
				}
			}
		}
		
		//order the snap objects and select the active one
		if(snapObjects.size() > 1) {
			Collections.sort(snapObjects);
		}
		int activeSnapObject = snapObjects.isEmpty() ? -1 : 0;
		editManager.setActiveSnapObject(activeSnapObject);
		
		editManager.getEditLayer().notifyContentChange();
	}
	
		/** This method tests if the mouse hit the virtual node on this segment.
	 * 
	 * @param segment		The segment to test
	 * @param mouseMerc		The mouse point, in pmercator coordinates
	 * @param mercRadSq		The allowed radius for a hit, in mercator coordinates
	 * @return				A SnapVirtualNode, if there was a hit.
	 */
	private SnapVirtualNode testVirtualNodeHit(OsmSegment segment, Point2D mouseMerc,
			double mercRadSq) {
		
		double xCenter = (segment.getNode1().getPoint().getX() + segment.getNode2().getPoint().getX())/2;
		double yCenter = (segment.getNode1().getPoint().getY() + segment.getNode2().getPoint().getY())/2;
		double err2 = mouseMerc.distanceSq(xCenter,yCenter);
		if(err2 < mercRadSq) {
			return new SnapVirtualNode(segment,xCenter,yCenter,err2);
		}
		else {
			return null;
		}
	}
	
	private void loadHitWays(OsmSegment segment, Point2D mouseMerc, 
			double mercRadSq, List<SnapObject> snapObjects) {
		Point2D p1 = segment.getNode1().getPoint();
		Point2D p2 = segment.getNode2().getPoint();
		double err2 = Line2D.ptSegDistSq(p1.getX(),p1.getY(),p2.getX(),p2.getY(),
				mouseMerc.getX(),mouseMerc.getY());
		if(err2 < mercRadSq) {
			for(OsmWay way:segment.getOsmWays()) {
				snapObjects.add(new SnapWay(way,mouseMerc,err2));
			}
		}
	}
}
