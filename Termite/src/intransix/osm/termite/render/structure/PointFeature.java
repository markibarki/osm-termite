package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.osm.OsmNode;
import intransix.osm.termite.map.osm.OsmObject;
import intransix.osm.termite.map.theme.Style;
import intransix.osm.termite.map.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.Shape;

/**
 *
 * @author sutter
 */
public class PointFeature implements Feature {
	
	private final static double RADIUS_METERS = .5;
	
	private int localVersion = OsmObject.INVALID_LOCAL_VERSION;
	private OsmNode osmNode;
	private Style style;
	private Shape marker;
	
	public PointFeature(OsmNode osmNode) {
		this.osmNode = osmNode;
	}
	
	public OsmNode getNode() {
		return osmNode;
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}
	
	public Style getStyle() {
		return style;
	}
	
	public void render(Graphics2D g2, AffineTransform mercatorToLocal, double zoomScale, Theme theme) {
		
		if(osmNode.getDataVersion() != this.localVersion) {
			//load geometry
			updateData(mercatorToLocal);

			//get the style
			style = theme.getStyle(osmNode);
			
			this.localVersion = osmNode.getDataVersion();
		}
		
		if((marker != null)&&(style != null)) {
			
			//load style params
			Color fillColor = style.getBodyColor();

			//render the object	
			if(fillColor != null) {
				g2.setPaint(fillColor);
				g2.fill(marker);
			}
			
		}
	}
	
	@Override
	public void transform(AffineTransform oldLocalToNewLocal) {
		if(this.marker != null) {
			marker = oldLocalToNewLocal.createTransformedShape(marker);
		}
	}
	
	void updateData(AffineTransform mercatorToLocal) {
		//update this object
		Point2D localPoint = new Point2D.Double();
		mercatorToLocal.transform(osmNode.getPoint(), localPoint);
		marker = new Ellipse2D.Double(localPoint.getX()-RADIUS_METERS,
				localPoint.getY()-RADIUS_METERS,2*RADIUS_METERS,2*RADIUS_METERS);
	}	
		
}
