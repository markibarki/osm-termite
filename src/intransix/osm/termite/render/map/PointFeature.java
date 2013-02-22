package intransix.osm.termite.render.map;

import intransix.osm.termite.app.feature.FeatureData;
import intransix.osm.termite.app.filter.FilterManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.PiggybackData;
import intransix.osm.termite.map.theme.Style;
import intransix.osm.termite.map.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.Shape;

import javafx.scene.shape.Circle;

/**
 *
 * @author sutter
 */
public class PointFeature extends Circle implements Feature {
	
	private final static double RADIUS_PIXELS = 3;
	
	private OsmNode osmNode;
	private Style style;
	private Ellipse2D marker = new Ellipse2D.Double();
	private Point2D point = new Point2D.Double();
	
	public FeatureInfo getFeatureInfo() {
		FeatureData data = (FeatureData)(osmNode.getPiggybackData(RenderLayer.piggybackIndexFeature));
		if(data != null) {
			return data.getFeatureInfo();
		}
		else {
			return null;
		}
	}
	
	public void initStyle(Theme theme) {
		Style style = theme.getStyle(osmNode);
		style.loadStyle(this);
	}
	
	public PointFeature(OsmNode osmNode) {
		this.osmNode = osmNode;
	}
	
	public OsmNode getNode() {
		return osmNode;
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
//		if(!FilterManager.getObjectRenderEnabled(osmNode)) return;
//		
//		if(!isUpToDate(osmNode)) {
//			//load geometry
//			updateData(mercatorToLocal);
//
//			//get the style
//			style = theme.getStyle(osmNode);
//			
//			markAsUpToDate(osmNode);
//		}
//		
//		if((marker != null)&&(style != null)) {
//			
//			//load style params
//			Color fillColor = style.getBodyColor();
//
//			//render the object	
//			if(fillColor != null) {
//				marker.setFrame(point.getX()-RADIUS_PIXELS,point.getY()-RADIUS_PIXELS,
//						2*RADIUS_PIXELS,2*RADIUS_PIXELS);
//				g2.setPaint(fillColor);
//				g2.fill(marker);
//			}
//			
//		}
//	}
	
//	@Override
//	public void transform(AffineTransform oldLocalToNewLocal) {
//		oldLocalToNewLocal.transform(point, point);
//	}
	
	void updateData() {
		//update this object
		Point2D point = osmNode.getPoint();
		setCenterX(point.getX());
		setCenterY(point.getY());
		this.setRadius(.0000000001);	
	}	
		
}
