package intransix.osm.termite.render.map;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.theme.Style;
import intransix.osm.termite.map.theme.Theme;
import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.awt.BasicStroke;

/**
 *
 * @author sutter
 */
public class PathFeature implements Feature {
	//====================
	// Private Proeprties
	//====================
	
	//virtual node hard coded specs - update this!!!
	private final static int RADIUS_PIX = 3;
	private final static Color virtualColor = Color.LIGHT_GRAY;
	private final static Stroke virtualStroke = new BasicStroke(1);
	
	private int localVersion = OsmData.INVALID_DATA_VERSION;
	private OsmWay osmWay;
	private Shape shape;
	private boolean isArea;
	private Style style;
	
	private List<Rectangle2D> virtualNodes = new ArrayList<Rectangle2D>();
	
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
		
		if(!osmWay.renderEnabled()) return;
		
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
		
		//draw virtual nodes
		g2.setColor(virtualColor);
		g2.setStroke(virtualStroke);
		for(Rectangle2D rect:virtualNodes) {
			g2.draw(rect);
		}
	}
	
	@Override
	public void transform(AffineTransform oldLocalToNewLocal) {
		if(this.shape != null) {
			shape = oldLocalToNewLocal.createTransformedShape(shape);
		}
		
		if(this.virtualNodes != null) {
			Point2D point = new Point2D.Double();
			for(Rectangle2D rect:virtualNodes) {
				point.setLocation(rect.getCenterX(),rect.getCenterY());
				oldLocalToNewLocal.transform(point, point);
				rect.setFrame(point.getX()-RADIUS_PIX,point.getY()-RADIUS_PIX,2*RADIUS_PIX,2*RADIUS_PIX);
			}
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
		
		
		//create the path object for the shape
		Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		//clear the virtual nodes
		virtualNodes.clear();
		
		if(multipoly != null) {
			//-----------------
			// Multipoly Case
			//-----------------
			List<OsmMember> members = multipoly.getMembers();
			
			if(members.get(0).osmObject == osmWay) {
				//main way
				
				this.isArea = osmWay.getIsArea();

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

			addWayToPath(path,osmWay,isArea, mercatorToLocal);
			
			this.shape = path;
		}
	}
	
	//======================
	// Private Methods
	//======================
	
	private void addWayToPath(Path2D path, OsmWay way, boolean isArea, AffineTransform mercatorToLocal) {
		
		//path working variables
		boolean started = false;
		Point2D temp = new Point2D.Double();
		
		//virtual node working variables
		double oldX = 0;
		double oldY = 0;
		double middleX;
		double middleY;
		
		for(OsmNode node:way.getNodes()) {
			if(true /* check if node is visible here */) {
				mercatorToLocal.transform(node.getPoint(),temp);
				
				if(started) {
					//path lineto instruction
					path.lineTo(temp.getX(),temp.getY());
					
					//create the virtual node
					middleX = (temp.getX() + oldX)/2;
					middleY = (temp.getY() + oldY)/2;
					addVirtualNode(middleX,middleY);
					
				}
				else {
					//move instructione
					path.moveTo(temp.getX(),temp.getY());
					started = true;
				}
				
				//store for virtual nodes
				oldX = temp.getX();
				oldY = temp.getY();
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
	
	/** This method adds a virtual node. */
	private void addVirtualNode(double x, double y) {
		virtualNodes.add(new Rectangle2D.Double(x-RADIUS_PIX,y-RADIUS_PIX,2*RADIUS_PIX,2*RADIUS_PIX));
	}
	
}
