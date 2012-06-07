package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.model.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.theme.Style;
import intransix.osm.termite.theme.Theme;
import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.ArrayList;

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
	private ArrayList<TermiteLevel> termiteLevels = new ArrayList<TermiteLevel>();
	private Path2D[] paths;
	private Style style;
	boolean isArea;
	
	public PathFeature(TermiteWay termiteWay) {
		this.termiteWay = termiteWay;
		this.osmWay = termiteWay.getOsmWay();
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
	
	public void render(Graphics2D g2, double zoomScale, Theme theme, TermiteLevel level) {
		
		if(osmWay.getLocalVersion() != this.localVersion) {
			
			//load geometry
			updateData();
			
			//get the style
			style = theme.getStyle(osmWay);
			
			this.localVersion = osmWay.getLocalVersion();
		}
		
		Shape shape = this.getLevelShape(level);
		
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

		TermiteMultiPoly multiPoly = termiteWay.getMultiPoly();
		
		if(multiPoly != null) {
			//-----------------
			// Multipoly Case
			//-----------------
			//only create multipoly paths if this is the main way 
			if(multiPoly.getMainWay() == termiteWay) {
				//main way
				
				termiteLevels.clear();
				for(TermiteWay way:multiPoly.getWays()) {
					//load all the levels
					for(TermiteLevel level:way.getLevels()) {
						if(!termiteLevels.contains(level)) {
							termiteLevels.add(level);
						}
					}
				}
				this.paths = new Path2D[termiteLevels.size()];
				this.isArea = termiteWay.getIsArea();
				
				//create the paths, using all ways in relation
				int index = 0;
				for(TermiteLevel level:termiteLevels) {
					Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);

					for(TermiteWay tWay:multiPoly.getWays()) {
						OsmWay osmWay = tWay.getOsmWay();
						addWayToPathForLevel(level,path,osmWay,isArea);
					}

					//add the path in the order of the level
					paths[index++] = path;
				}
			
			}
			else {
				//non-main way
				termiteLevels.clear();
				paths = new Path2D[0];
			}
		}
		else {
			//-----------------
			// Normal Way Case
			//-----------------
			
			//update this object
			this.termiteLevels.addAll(termiteWay.getLevels());
			this.paths = new Path2D[termiteLevels.size()];
			this.isArea = termiteWay.getIsArea();
		
			//create the paths
			int index = 0;
			for(TermiteLevel level:termiteLevels) {
				Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);

				addWayToPathForLevel(level,path,osmWay,isArea);

				//add the path in the order of the level
				paths[index++] = path;
			}
		}
	}
	
	Shape getLevelShape(TermiteLevel level) {	
		int index = termiteLevels.indexOf(level);
		if(index < 0) return null;
		
		//the order should match, make sure the length is enough
		if(paths.length > index) {
			return paths[index];
		}
		else {
			return null;
		}
	}
	
	static void addWayToPathForLevel(TermiteLevel level, Path2D path, OsmWay way, boolean isArea) {
		boolean started = false;
		for(OsmNode oNode:way.getNodes()) {
			TermiteNode tNode = oNode.getTermiteNode();
			if(tNode.getLevel() == level) {
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
		if(isArea) {
			path.closePath();
		}
	}
	
	//======================
	// Private Methods
	//======================
	
}
