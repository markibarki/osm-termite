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
	private double pixelsToLocal = 1.0;
	private double radiusPixels = 0.0;
	
	public FeatureInfo getFeatureInfo() {
		return RenderLayer.getObjectFeatureInfo(osmNode);
	}
	
	public void initStyle(Theme theme) {
		Style style = theme.getStyle(osmNode);
		style.loadPointStyle(this);
	}
	
	public void setPixelsToLocal(double pixelsToLocal) {
		this.pixelsToLocal = pixelsToLocal;
		this.setRadius(radiusPixels * pixelsToLocal);
	}
	
	public void setRadiusPixels(double radiusPixels) {
		this.radiusPixels = radiusPixels;
		this.setRadius(radiusPixels * pixelsToLocal);
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
	
	void updateData(AffineTransform mercToLocal) {
		//update this object
		Point2D mercPoint = osmNode.getPoint();
		Point2D localPoint = new Point2D.Double();
		mercToLocal.transform(mercPoint, localPoint);
		setCenterX(localPoint.getX());
		setCenterY(localPoint.getY());
		this.setRadius(RADIUS_PIXELS);	
	}	
		
}
