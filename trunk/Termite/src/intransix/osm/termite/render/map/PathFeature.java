package intransix.osm.termite.render.map;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmModel;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.OsmRelation;
import intransix.osm.termite.map.data.OsmMember;
import intransix.osm.termite.map.theme.Style;
import intransix.osm.termite.map.theme.Theme;
import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.*;

/**
 *
 * @author sutter
 */
public class PathFeature implements Feature {
	//====================
	// Private Proeprties
	//====================
	
	private int localVersion = OsmData.INVALID_DATA_VERSION;
	private OsmWay osmWay;
	private Shape shape;
	private boolean isArea;
	private Style style;
	
	public PathFeature(OsmWay osmWay) {
		this.osmWay = osmWay;
	}
	
	public OsmWay getWay() {
		return osmWay;
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}
	
	public Style getStyle() {
		return style;
	}
	
	public void render(Graphics2D g2, AffineTransform mercatorToLocal, double zoomScale, Theme theme) {
		
		if(osmWay.getDataVersion() != this.localVersion) {			
			//load geometry
			updateData(mercatorToLocal);
			
			//get the style
			style = theme.getStyle(osmWay);
			
			this.localVersion = osmWay.getDataVersion();
		}
		
		if((shape != null)&&(style != null)) {
			
			//load style params
			Color fillColor = null;
			Color strokeColor = null;
			Stroke stroke = null;
			if(isArea) {
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
	
	@Override
	public void transform(AffineTransform oldLocalToNewLocal) {
		if(this.shape != null) {
			shape = oldLocalToNewLocal.createTransformedShape(shape);
		}
	}
	
	void updateData(AffineTransform mercatorToLocal) {

		//check if this is the member in a multipolygon
		OsmRelation multipoly = null;
		for(OsmRelation relation:osmWay.getRelations()) {
			if(OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relation.getRelationType())) {
				multipoly = relation;
				break;
			}
		}
		
		if(multipoly != null) {
			//-----------------
			// Multipoly Case
			//-----------------
			List<OsmMember> members = multipoly.getMembers();
			
			if(members.get(0).osmObject == osmWay) {
				//main way
				
				this.isArea = osmWay.getIsArea();
				Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);

				for(OsmMember member:members) {
					if(member.osmObject instanceof OsmWay) {
						addWayToPath(path,(OsmWay)member.osmObject,isArea, mercatorToLocal);
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
			this.isArea = osmWay.getIsArea();
		
			//create the paths
			Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);

			addWayToPath(path,osmWay,isArea, mercatorToLocal);
			
			this.shape = path;
		}
	}
	
	static void addWayToPath(Path2D path, OsmWay way, boolean isArea, AffineTransform mercatorToLocal) {
		boolean started = false;
		Point2D temp = new Point2D.Double();
		for(OsmNode node:way.getNodes()) {
			if(true /* check if node is visible here */) {
				mercatorToLocal.transform(node.getPoint(),temp);
				
				if(started) {
					path.lineTo(temp.getX(),temp.getY());
				}
				else {
					path.moveTo(temp.getX(),temp.getY());
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
