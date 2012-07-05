package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.util.GraduatedList;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.gui.MapDataListener;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class EditLayer extends MapLayer implements MapDataListener, 
		MouseListener, MouseMotionListener, KeyListener {
	
	private final static double RADIUS_PIXELS = 5; 
	private final static float SELECT_WIDTH = 3;
	private final static float HOVER_PRESELECT_WIDTH = 4;
	private final static float HOVER_OTHER_WIDTH = 2;
	
	private final static Color SELECT_COLOR = Color.RED;
	private final static Color HOVER_PRESELECT_COLOR = Color.MAGENTA;
	private final static Color HOVER_OTHER_COLOR = Color.PINK;
	private final static BasicStroke SELECT_STROKE = new BasicStroke(SELECT_WIDTH);
	private final static BasicStroke HOVER_PRESELECT_STROKE = new BasicStroke(HOVER_PRESELECT_WIDTH);
	private final static BasicStroke HOVER_OTHER_STROKE = new BasicStroke(HOVER_OTHER_WIDTH);
	
	private OsmData osmData;
	
	private List<OsmNode> hoveredNodes = new ArrayList<OsmNode>();
	private int preselectNode;
	private List<OsmWay> hoveredWays = new ArrayList<OsmWay>();
	private int preselectWay;
	
	private List<OsmObject> selection = new ArrayList<OsmObject>();
	
	public void onMapData(OsmData osmData) {
		this.osmData = osmData;
	}
	
	@Override
	public void setActiveState(boolean isActive) {
		super.setActiveState(isActive);
		MapPanel mapPanel = this.getMapPanel();
		if(mapPanel != null) {
			if(isActive) {
				mapPanel.addMouseListener(this);
				mapPanel.addMouseMotionListener(this);
				mapPanel.addKeyListener(this);
			}
			else {
				mapPanel.removeMouseListener(this);
				mapPanel.removeMouseMotionListener(this);
				mapPanel.removeKeyListener(this);
			}
		}
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mercatorToPixels = getMapPanel().getMercatorToPixels();		
		
		//some working variables
		Rectangle2D rect = new Rectangle2D.Double();
		Line2D line = new Line2D.Double();
		Point2D pixXY = new Point2D.Double();
		Point2D prevPixXY = new Point2D.Double();
		int index;
		
		//render hover
		if(!hoveredNodes.isEmpty()) {
			index = 0;
			for(OsmNode node:hoveredNodes) {
				//get proper render style
				if(index++ == preselectNode) {
					g2.setColor(HOVER_PRESELECT_COLOR);
				}
				else {
					g2.setColor(HOVER_OTHER_COLOR);
				}
				//render
				renderNode(g2,node,mercatorToPixels,pixXY,rect);
			}
		}
		else if(!hoveredWays.isEmpty()) {
			index = 0;
			for(OsmWay way:hoveredWays) {
				//get proper render style
				if(index++ == preselectWay) {
					g2.setColor(HOVER_PRESELECT_COLOR);
					g2.setStroke(HOVER_PRESELECT_STROKE);
				}
				else {
					g2.setColor(HOVER_OTHER_COLOR);
					g2.setStroke(HOVER_OTHER_STROKE);
				}
				//render
				renderWay(g2,way,mercatorToPixels,pixXY,prevPixXY,line);
			}
		}
		
		//render selection
		if(!selection.isEmpty()) {
			g2.setColor(SELECT_COLOR);
			g2.setStroke(SELECT_STROKE);
			for(OsmObject osmObject:selection) {
				if(osmObject instanceof OsmNode) {
					renderNode(g2,(OsmNode)osmObject,mercatorToPixels,pixXY,rect);
				}
				else if(osmObject instanceof OsmWay) {
					renderWay(g2,(OsmWay)osmObject,mercatorToPixels,pixXY,prevPixXY,line);
				}
			}
		}
	}
	
	private void renderNode(Graphics2D g2, OsmNode node, 
			AffineTransform mercatorToPixels, Point2D pixXY, Rectangle2D rect) {
		mercatorToPixels.transform(node.getPoint(),pixXY);
		rect.setRect(pixXY.getX()-RADIUS_PIXELS, pixXY.getY() - RADIUS_PIXELS,
				2*RADIUS_PIXELS, 2*RADIUS_PIXELS);
		g2.fill(rect);
	}
	
	private void renderWay(Graphics2D g2, OsmWay way, 
			AffineTransform mercatorToPixels, Point2D pixXY, Point2D prevPixXY, Line2D line) {
		
		boolean started = false;
		for(OsmNode node:way.getNodes()) {
			mercatorToPixels.transform(node.getPoint(),pixXY);
			if(started) {
				line.setLine(pixXY,prevPixXY);
				g2.draw(line);
			}
			else {
				started = true;
			}
			prevPixXY.setLocation(pixXY);
		}
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
		this.getMapPanel().requestFocusInWindow();
	}
	
	public void mouseExited(MouseEvent e) {
		hoveredNodes.clear();
		hoveredWays.clear();
		preselectNode = 0;
		preselectWay = 0;
		getMapPanel().repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		boolean prevHit = !((hoveredNodes.isEmpty())&&(hoveredWays.isEmpty()));
		hoveredNodes.clear();
		hoveredWays.clear();
		preselectNode = 0;
		preselectWay = 0;
		
		//read mouse location in global coordinates
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc, mouseMerc);
		double scalePixelsPerMerc = mapPanel.getZoomScalePixelsPerMerc();
		double mercRad = RADIUS_PIXELS / scalePixelsPerMerc;
		double mercRadSq = mercRad * mercRad;
		
		
		//loook for a point
		GraduatedList<OsmObject> objectList = osmData.getOrderedList();
		for(java.util.List<OsmObject> subList:objectList.getLists()) {
			for(OsmObject mapObject:subList) {
				//make sure edit is enabled for this object
				if(!mapObject.editEnabled()) continue;
				
				//check for hit
				if(mapObject instanceof OsmNode) {
					if(nodeHit((OsmNode)mapObject,mouseMerc,mercRadSq)) {
						hoveredNodes.add((OsmNode)mapObject);
					}
				}
				else if(mapObject instanceof OsmWay) {
					if(wayHit((OsmWay)mapObject,mouseMerc,mercRadSq)) {
						hoveredWays.add((OsmWay)mapObject);
					}
				}
			}			
		}
		boolean hit = !((hoveredNodes.isEmpty())&&(hoveredWays.isEmpty()));
		preselectNode = hoveredNodes.size() - 1;
		preselectWay = hoveredWays.size() - 1;
		//repaint if there is a hit or if the hit status changes
		if((hit)||(prevHit != hit)) {
			mapPanel.repaint();
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if(!hoveredNodes.isEmpty()) {
			OsmNode node = hoveredNodes.get(preselectNode);
			if(!e.isShiftDown()) {
				selection.clear();
			}
			selection.add(node);
		}
		else if(!hoveredWays.isEmpty()) {
			OsmWay way = hoveredWays.get(preselectWay);
			if(!e.isShiftDown()) {
				selection.clear();
			}
			selection.add(way);
		}
		else {
			if(!e.isShiftDown()) {
				selection.clear();
			}
		}
		getMapPanel().repaint();
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	//------------------------
	// Key Event
	//------------------------
	
	/** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
		boolean changed = false;
		if((e.getKeyCode() == KeyEvent.VK_LEFT)||(e.getKeyCode() == KeyEvent.VK_UP)) {
			if(!hoveredNodes.isEmpty()) {
				preselectNode--;
				if(preselectNode < 0) preselectNode = hoveredNodes.size() - 1;
				changed = true;
			}
			else if(!hoveredWays.isEmpty()) {
				preselectWay--;
				if(preselectWay < 0) preselectWay = hoveredWays.size() - 1;
				changed = true;
			}
		}
		else if((e.getKeyCode() == KeyEvent.VK_RIGHT)||(e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(!hoveredNodes.isEmpty()) {
				preselectNode++;
				if(preselectNode >= hoveredNodes.size()) preselectNode = 0;
				changed = true;
			}
			else if(!hoveredWays.isEmpty()) {
				preselectWay++;
				if(preselectWay >= hoveredWays.size()) preselectWay = 0;
				changed = true;
			}
		}
		
		if(changed) {
			getMapPanel().repaint();
		}
    }

    /** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
    }
	
	//============================
	// Private Methods
	//============================
	
	//-----------------------
	// Hit Methods
	//-----------------------
	
	/** This returns true if the node was hit. */
	private boolean nodeHit(OsmNode node, Point2D mouseMerc, double mercRadSq) {
		return (mouseMerc.distanceSq(node.getPoint()) < mercRadSq);
	}
	
	/** This returns true if the way was hit. */
	private boolean wayHit(OsmWay way, Point2D mouseMerc, double mercRadSq) {
		OsmNode prevNode = null;
		for(OsmNode node:way.getNodes()) {
			if(prevNode != null) {
				if(segmentHit(node,prevNode,mouseMerc,mercRadSq)) {
					return true;
				}
			}
			prevNode = node;
		}
		return false;
	}
	
	/** This returns true if a segment was hit. */
	private boolean segmentHit(OsmNode node1, OsmNode node2, Point2D mercPoint, double mercRadSq) {
		Point2D p1 = node1.getPoint();
		Point2D p2 = node2.getPoint();
		return Line2D.ptSegDistSq(p1.getX(),p1.getY(),p2.getX(),p2.getY(),mercPoint.getX(),mercPoint.getY()) < mercRadSq;
	}
}
