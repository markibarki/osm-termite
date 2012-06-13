package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.model.*;
import intransix.osm.termite.map.osm.OsmNode;
import intransix.osm.termite.map.osm.OsmObject;
import intransix.osm.termite.theme.Style;
import intransix.osm.termite.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author sutter
 */
public class PointFeature {
	
	private final static double RADIUS_METERS = .5;
	
	private int localVersion = OsmObject.INVALID_LOCAL_VERSION;
	private TermiteNode termiteNode;
	private OsmNode osmNode;
	private TermiteLevel termiteLevel;
	private Style style;
	private Ellipse2D marker;
	
	public PointFeature(TermiteNode termiteNode) {
		this.termiteNode = termiteNode;
		this.osmNode = termiteNode.getOsmObject();
	}
	
	public TermiteNode getNode() {
		return termiteNode;
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}
	
	public Style getStyle() {
		return style;
	}
	
	public void render(Graphics2D g2, double zoomScale, Theme theme, TermiteLevel level) {
		
//		if(osmNode.getLocalVersion() != this.localVersion) {
if(termiteNode.getTermiteLocalVersion() != this.localVersion) {
			//load geometry
			updateData();

			//get the style
			style = theme.getStyle(osmNode);
			
//			this.localVersion = osmNode.getLocalVersion();
this.localVersion = termiteNode.getTermiteLocalVersion();
		}
		
		if((marker != null)&&(style != null)&&(termiteLevel == level)) {
			
			//load style params
			Color fillColor = style.getBodyColor();

			//render the object	
			if(fillColor != null) {
				g2.setPaint(fillColor);
				g2.fill(marker);
			}
			
		}
	}
	
	void updateData() {
		//update this object
		this.termiteLevel = termiteNode.getLevel();
		marker = new Ellipse2D.Double(osmNode.getX()-RADIUS_METERS,
				osmNode.getY()-RADIUS_METERS,2*RADIUS_METERS,2*RADIUS_METERS);
	}	
		
}
