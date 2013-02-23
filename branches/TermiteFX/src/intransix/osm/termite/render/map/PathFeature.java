package intransix.osm.termite.render.map;

import intransix.osm.termite.app.feature.FeatureData;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.theme.Style;

import javafx.scene.shape.Path;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.ClosePath;

//import java.awt.Graphics2D;
//import java.awt.Shape;
//import java.awt.Stroke;
//import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
//import java.awt.geom.Rectangle2D;
//import java.awt.geom.AffineTransform;
import java.util.*;
import javafx.scene.shape.PathElement;

/**
 *
 * @author sutter
 */
public class PathFeature extends Path implements Feature {
	//====================
	// Private Proeprties
	//====================
	
	//virtual node hard coded specs - update this!!!
//	private final static int RADIUS_PIX = 3;
//	private final static Color virtualColor = Color.LIGHT_GRAY;
//	private final static Stroke virtualStroke = new BasicStroke(1);
	
	private OsmWay osmWay;
	private boolean isArea;
	private List<PathElement> workingElements = new ArrayList<>();
	private double pixelsToMerc = 1.0;
	private double strokeWidthPixels = 0.0;
	
//	private List<Rectangle2D> virtualNodes = new ArrayList<Rectangle2D>();
	
	public FeatureInfo getFeatureInfo() {
		return RenderLayer.getObjectFeatureInfo(osmWay);
	}
	
	public void initStyle(Theme theme) {
		Style style = theme.getStyle(osmWay);
		if(isArea) {
			style.loadAreaStyle(this);
		}
		else {
			style.loadLineStyle(this);
		}
	}
	
	public void setPixelsToMerc(double pixelsToMerc) {
		this.pixelsToMerc = pixelsToMerc;
		this.setStrokeWidth(strokeWidthPixels * pixelsToMerc);
	}
	
	public void setStrokeWidthPixels(double strokeWidthPixels) {
		this.strokeWidthPixels = strokeWidthPixels;
		this.setStrokeWidth(strokeWidthPixels * pixelsToMerc);
	}
	
	public PathFeature(OsmWay osmWay) {
		this.osmWay = osmWay;
	}
	
	public OsmWay getWay() {
		return osmWay;
	}
	
//	public void setStyle(Style style) {
//		this.style = style;
//	}
//	
//	public Style getStyle() {
//		return style;
//	}
	
//	public void render(Graphics2D g2, AffineTransform mercatorToLocal, double zoomScale, Theme theme) {
//		
//		if(!FilterManager.getObjectRenderEnabled(osmWay)) return;
//		
//		if(!isUpToDate(osmWay)) {			
//			//load geometry
//			updateData(mercatorToLocal);
//			
//			//get the style
//			style = theme.getStyle(osmWay);
//			
//			markAsUpToDate(osmWay);
//		}
//		
//		if((shape != null)&&(style != null)) {
//			
//			//load style params
//			Color fillColor = null;
//			Color strokeColor = null;
//			Stroke stroke = null;
//			if(isArea) {
//				fillColor = style.getBodyColor();
//				strokeColor = style.getOutlineColor();
//			}
//			else {
//				fillColor = null;
//				strokeColor = style.getBodyColor();
//			}
//			stroke = style.getStroke(zoomScale);
//			
//			//render the object	
//			if(fillColor != null) {
//				g2.setPaint(fillColor);
//				g2.fill(shape);
//			}
//			if((strokeColor != null)&&(stroke != null)) {
//				g2.setStroke(stroke);
//				g2.setColor(strokeColor);
//				g2.draw(shape);
//			}			
//		}
//		
//		//draw virtual nodes
//		g2.setColor(virtualColor);
//		g2.setStroke(virtualStroke);
//		for(Rectangle2D rect:virtualNodes) {
//			g2.draw(rect);
//		}
//	}
	
//	@Override
//	public void transform(AffineTransform oldLocalToNewLocal) {
//		if(this.shape != null) {
//			shape = oldLocalToNewLocal.createTransformedShape(shape);
//		}
//		
//		if(this.virtualNodes != null) {
//			Point2D point = new Point2D.Double();
//			for(Rectangle2D rect:virtualNodes) {
//				point.setLocation(rect.getCenterX(),rect.getCenterY());
//				oldLocalToNewLocal.transform(point, point);
//				rect.setFrame(point.getX()-RADIUS_PIX,point.getY()-RADIUS_PIX,2*RADIUS_PIX,2*RADIUS_PIX);
//			}
//		}
//	}
//	
	void updateData() {

		//check if this is the member in a multipolygon
		OsmRelation multipoly = null;
		for(OsmRelation relation:osmWay.getRelations()) {
			if(OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relation.getRelationType())) {
				multipoly = relation;
				break;
			}
		}
		
		
		//create the path object for the shape
		workingElements.clear();
		
		//clear the virtual nodes
//		virtualNodes.clear();
		
//		if(multipoly != null) {
//			//-----------------
//			// Multipoly Case
//			//-----------------
//			List<OsmMember> members = multipoly.getMembers();
//			PathFeature currentFeature;
//			OsmWay currentWay;
//			boolean firstDone = false;
//			
//			for(OsmMember member:members) {
//				if(member.osmObject instanceof OsmWay) {
//					currentWay = (OsmWay)member.osmObject;
//					
//					//get the feature for this compononent of the multipoly
//					currentFeature = (PathFeature)currentWay.getPiggybackData(RenderLayer.piggybackIndexRender);
//					if(currentFeature == null) {
//						currentFeature = new PathFeature(currentWay);
//						currentWay.setPiggybackData(RenderLayer.piggybackIndexRender, currentFeature);
//					}
//
//					//just use first as the main
//					//don't store the others
//					if(!firstDone) {
//						currentFeature.isArea = currentWay.getIsArea();
//						currentFeature.shape = path;
//						firstDone = true;
//					}
//					else {
//						currentFeature.shape = null;
//					}
//
//					//add this way to the multipolygon
//					if(member.osmObject instanceof OsmWay) {
//						addWayToPath(workingElements,(OsmWay)member.osmObject,isArea);
//					}
//					
//					//mark the feature as up to date
//					currentFeature.markAsUpToDate(currentWay);
//				}
//			}
//
//		}
//		else {
			//-----------------
			// Normal Way Case
			//-----------------
			
			//update this object
			this.isArea = osmWay.getIsArea();
			addWayToPath(workingElements,osmWay,isArea);
			
			this.getElements().setAll(workingElements);
			workingElements.clear();
//		}
	}
	
	//======================
	// Private Methods
	//======================
	
	private void addWayToPath(List<PathElement> elements, OsmWay way, boolean isArea) {
		
		//path working variables
		boolean started = false;
		Point2D mercPoint;
		
		//virtual node working variables
		double oldX = 0;
		double oldY = 0;
		double middleX;
		double middleY;
		
		for(OsmNode node:way.getNodes()) {
			if(true /* check if node is visible here */) {
				mercPoint = node.getPoint();
				
				if(started) {
					//path lineto instruction
					LineTo lineTo = new LineTo(mercPoint.getX(),mercPoint.getY());
					elements.add(lineTo);
					
					//create the virtual node
					middleX = (mercPoint.getX() + oldX)/2;
					middleY = (mercPoint.getY() + oldY)/2;
					addVirtualNode(middleX,middleY);
					
				}
				else {
					//move instructione
					MoveTo moveTo = new MoveTo(mercPoint.getX(),mercPoint.getY());
					elements.add(moveTo);
					started = true;
				}
				
				//store for virtual nodes
				oldX = mercPoint.getX();
				oldY = mercPoint.getY();
			}
			else {
				//if we go out of the level:
				//AREA; just skip nodes
				//LINE: end segment and do a move to next
				if(!isArea) {
					started = false;
				}
			}
		}
		//close the path if this is an area
		if((isArea)&&(started)) {
			elements.add(new ClosePath());
		}
	}
	
	/** This method adds a virtual node. */
	private void addVirtualNode(double x, double y) {
//@TODO need to reinsert virtual nodes with proper size (scale for pixels)
//		virtualNodes.add(new Rectangle(x-RADIUS_PIX,y-RADIUS_PIX,2*RADIUS_PIX,2*RADIUS_PIX));
	}
	
}
