package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.model.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.theme.Style;
import intransix.osm.termite.map.theme.Theme;
import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.*;

/**
 *
 * @author sutter
 */
public class PathFeature {
	//====================
	// Private Proeprties
	//====================
	
	private int localVersion = OsmObject.INVALID_LOCAL_VERSION;
	private TermiteWay termiteWay;
	private OsmWay osmWay;
	private Shape shape;
	private Style style;
	boolean isArea;
	
	public PathFeature(TermiteWay termiteWay) {
		this.termiteWay = termiteWay;
		this.osmWay = termiteWay.getOsmObject();
	}
	
	public TermiteWay getWay() {
		return termiteWay;
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}
	
	public Style getStyle() {
		return style;
	}
	
	public void render(Graphics2D g2, double zoomScale, Theme theme) {
		
//		if(osmWay.getLocalVersion() != this.localVersion) {
if(termiteWay.getDataVersion() != this.localVersion) {			
			//load geometry
			updateData();
			
			//get the style
			style = theme.getStyle(osmWay);
			
this.localVersion = termiteWay.getDataVersion();
//			this.localVersion = osmWay.getLocalVersion();
		}
		
		if((shape != null)&&(style != null)) {
			
			//load style params
			Color fillColor = null;
			Color strokeColor = null;
			Stroke stroke = null;
			if(termiteWay.getIsArea()) {
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
				g2.setPaint(fillColor);
				g2.fill(shape);
			}
			if((strokeColor != null)&&(stroke != null)) {
				g2.setStroke(stroke);
				g2.setColor(strokeColor);
				g2.draw(shape);
			}
			
			
		}
	}
	
	void updateData() {

		//check if this is the member in a multipolygon
		TermiteRelation multipoly = termiteWay.getMultipolygon();
		
		if(multipoly != null) {
			//-----------------
			// Multipoly Case
			//-----------------
			List<TermiteMember> members = multipoly.getMembers();
			
			if(members.get(0).termiteObject == termiteWay) {
				//main way
				
				this.isArea = termiteWay.getIsArea();
				Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);

				for(TermiteMember member:members) {
					if(member.termiteObject instanceof TermiteWay) {
						addWayToPath(path,(TermiteWay)member.termiteObject,isArea);
					}
				}
				
				this.shape = path;
			
			}
			else {
				this.shape = null;
			}
		}
		else {
			//-----------------
			// Normal Way Case
			//-----------------
			
			//update this object
			this.isArea = termiteWay.getIsArea();
		
			//create the paths
			Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);

			addWayToPath(path,termiteWay,isArea);
			
			this.shape = path;
		}
	}
	
	static void addWayToPath(Path2D path, TermiteWay way, boolean isArea) {
		boolean started = false;
		for(TermiteNode tNode:way.getNodes()) {
			if(true /* check if node is visible here */) {
				OsmNode oNode = tNode.getOsmObject();
				double x = oNode.getX();
				double y = oNode.getY();
				if(started) {
					path.lineTo(x,y);
				}
				else {
					path.moveTo(x,y);
					started = true;
				}
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
			path.closePath();
		}
	}
	
	//======================
	// Private Methods
	//======================
	
}
