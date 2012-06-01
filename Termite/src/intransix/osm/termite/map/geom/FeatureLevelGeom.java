package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.prop.FeatureInfo;
import intransix.osm.termite.theme.Style;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;
import intransix.osm.termite.theme.Theme;

/**
 *
 * @author sutter
 */
public class FeatureLevelGeom implements Comparable<FeatureLevelGeom> {
	
	//temporary
	private final static double RADIUS = .00004;
	
	private TermiteFeature feature;
	private TermiteLevel level;
	private Shape shape;
	
	public FeatureLevelGeom(TermiteFeature feature, TermiteLevel level) {
		this.feature = feature;
		this.level = level;
	}
	
	public TermiteLevel getLevel() {
		return level;
	}
	
	public TermiteFeature getFeature() {
		return feature;
	}

	/** renders the feature. */
	public void render(Graphics2D g, float zoomScale, Theme theme) {

		if(feature.getIsDirty()) {
			//classify feature
			feature.classify();
			
			//load geometry
			boolean success = createFeatureGeom();
			if(!success) return;
			
			//get the style
			theme.loadStyle(feature);
		}
			
		Style style = feature.getStyle();
		if(style == null) return;
		
		if((shape != null)&&(style != null)) {
			
			//load style params
			Color fillColor = null;
			Color strokeColor = null;
			Stroke stroke = null;
			if(feature.getIsArea()) {
				fillColor = style.getBodyColor();
				strokeColor = style.getOutlineColor();
			}
			else {
				fillColor = null;
				strokeColor = style.getBodyColor();
			}
			stroke = style.getStroke(zoomScale);
			
			//render the object	
			if(fillColor != null) {
				g.setPaint(fillColor);
				g.fill(shape);
			}
			if((strokeColor != null)&&(stroke != null)) {
				g.setStroke(stroke);
				g.setColor(strokeColor);
				g.draw(shape);
			}
			
			
		}
	}
	
	@Override
	public int compareTo(FeatureLevelGeom other) {
		FeatureInfo otherFeatureInfo = other.feature.getFeatureInfo();
		FeatureInfo thisFeatureInfo = this.feature.getFeatureInfo();
		int otherZorder;
		int thisZorder;
		if(otherFeatureInfo == null) {
			otherZorder = FeatureInfo.DEFAULT_ZORDER;
		}
		else {
			otherZorder = otherFeatureInfo.getZorder();
		}
		if(thisFeatureInfo == null) {
			thisZorder = FeatureInfo.DEFAULT_ZORDER;
		}
		else {
			thisZorder = thisFeatureInfo.getZorder();
		}
		return (thisZorder - otherZorder);	
	}
	
	/** This method creates the geometry for the shape. */
	private boolean createFeatureGeom() {
		shape = null;
		
		int nodeCount = 0;
		for(TermiteWay way:feature.getWays()) {
			nodeCount += way.getNodes().size();
		}
		
		if(nodeCount == 1) {
			for(TermiteWay way:feature.getWays()) {
				ArrayList<TermiteNode> nodes = way.getNodes();
				if(!nodes.isEmpty()) {
					TermiteNode node = nodes.get(0);
					Point2D point = node.getPoint();
					shape = new Ellipse2D.Double(point.getX(),point.getY(),RADIUS,RADIUS);
					break;
				}
			}
			return true;
		}
		else {
			Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
			for(TermiteWay way:feature.getWays()) {
				boolean started = false;
				for(TermiteNode node:way.getNodes()) {
					//get this segment of the path
					Point2D point = node.getPoint();
					if(point != null) {
						if(started) {
							path.lineTo(point.getX(),point.getY());
						}
						else {
							path.moveTo(point.getX(),point.getY());
							started = true;
						}
					}	
				}
				//close the path if this is an area
				if(feature.getIsArea()) {
					path.closePath();
				}
			}
			this.shape = path;
			return true;
		}
	}
}
