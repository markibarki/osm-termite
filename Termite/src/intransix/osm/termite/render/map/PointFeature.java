package intransix.osm.termite.render.map;

import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmData;
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
	
	private final static double RADIUS_PIXELS = 3;
	
	private int localVersion = OsmData.INVALID_DATA_VERSION;
	private OsmNode osmNode;
	private Style style;
	private Ellipse2D marker = new Ellipse2D.Double();
	private Point2D point = new Point2D.Double();
	
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
		
		if(!osmNode.renderEnabled()) return;
		
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
				marker.setFrame(point.getX()-RADIUS_PIXELS,point.getY()-RADIUS_PIXELS,
						2*RADIUS_PIXELS,2*RADIUS_PIXELS);
				g2.setPaint(fillColor);
				g2.fill(marker);
			}
			
		}
	}
	
	@Override
	public void transform(AffineTransform oldLocalToNewLocal) {
		oldLocalToNewLocal.transform(point, point);
	}
	
	void updateData(AffineTransform mercatorToLocal) {
		//update this object
		mercatorToLocal.transform(osmNode.getPoint(), point);
	}	
		
}
