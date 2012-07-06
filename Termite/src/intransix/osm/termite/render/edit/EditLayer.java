package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.util.GraduatedList;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.gui.*;
import intransix.osm.termite.gui.stdmode.*;
import intransix.osm.termite.map.data.edit.*;
import intransix.osm.termite.map.feature.FeatureInfo;
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
public class EditLayer extends MapLayer implements MapDataListener, FeatureLayerListener,  
		MouseListener, MouseMotionListener, KeyListener {
	
	public enum EditMode {
		SelectTool,
		NodeTool,
		WayTool,
		Other
	}
	
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
	private EditManager editManager;
	private EditMode editMode;
	private FeatureInfo featureInfo;

	
	private List<OsmNode> hoveredNodes = new ArrayList<OsmNode>();
	private int preselectNode;
	private List<OsmWay> hoveredWays = new ArrayList<OsmWay>();
	private int preselectWay;
	private List<NodeSegment> hoveredSegments = new ArrayList<NodeSegment>();
	private int preselectSegment;
	
	private List<OsmObject> selection = new ArrayList<OsmObject>();
	
	private List<EditNode> virtualNodes = new ArrayList<EditNode>();
	private List<EditSegment> virtualSegments = new ArrayList<EditSegment>();
	
//set these up!!!
	private EditDestPoint moveStartPoint;
	private boolean inMove;
	private OsmWay activeWay;
	private boolean isEnd;
	
	public void onMapData(OsmData osmData) {
		this.osmData = osmData;
		editManager = new EditManager(osmData);
	}
	
	public void setEditMode(EditorMode editorMode) {
		if(editorMode instanceof SelectEditorMode) {
			editMode = EditMode.SelectTool;
			clearSelection();
		}
		else if(editorMode instanceof NodeEditorMode) {
			editMode = EditMode.NodeTool;
			clearSelection();
		}
		else if(editorMode instanceof WayEditorMode) {
			editMode = EditMode.WayTool;
			if(selection.size() == 1) {
				OsmObject osmObject = selection.get(0);
				if(osmObject instanceof OsmWay) {
					activeWay = (OsmWay)osmObject;
					isEnd = true;
				}
				else {
					clearSelection();
				}
			}
			else {
				clearSelection();
			}
		}
		else {
			editMode = EditMode.Other;
			clearSelection();
		}
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
		else if(!hoveredSegments.isEmpty()) {
			index = 0;
			for(NodeSegment ns:hoveredSegments) {
				//get proper render style
				if(index++ == preselectSegment) {
					g2.setColor(HOVER_PRESELECT_COLOR);
					g2.setStroke(HOVER_PRESELECT_STROKE);
				}
				else {
					g2.setColor(HOVER_OTHER_COLOR);
					g2.setStroke(HOVER_OTHER_STROKE);
				}
				//render
				renderSegment(g2,ns,mercatorToPixels,pixXY,prevPixXY,line);
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
	
	private void renderSegment(Graphics2D g2, NodeSegment ns, 
			AffineTransform mercatorToPixels, Point2D pixXY, Point2D prevPixXY, Line2D line) {
		
		mercatorToPixels.transform(ns.node1.getPoint(),prevPixXY);
		mercatorToPixels.transform(ns.node2.getPoint(),pixXY);
		line.setLine(pixXY,prevPixXY);
		g2.draw(line);
	}
	
	/** This method is called when a feature layer is selected. It may be called 
	 * with the value null if a selection is cleared an no new selection is made. 
	 * 
	 * @param featureInfo	The selected feature type
	 */
	public void onFeatureLayerSelected(FeatureInfo featureInfo) {
		this.featureInfo = featureInfo;
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
		clearHover();
	}
	
	public void mouseMoved(MouseEvent e) {
		boolean prevHit = !((hoveredNodes.isEmpty())&&(hoveredWays.isEmpty())&&(hoveredSegments.isEmpty()));
		hoveredNodes.clear();
		hoveredWays.clear();
		hoveredSegments.clear();
		
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
					if((editMode == EditMode.SelectTool)&&(!inMove)) {
						//check for hitting ways
						if(wayHit((OsmWay)mapObject,mouseMerc,mercRadSq)) {
							hoveredWays.add((OsmWay)mapObject);
						}
					}
					else {
						//check for hitting segments
						final NodeSegment segmentHit = getHitSegment((OsmWay)mapObject,mouseMerc,mercRadSq);
						if(segmentHit != null) {
							hoveredSegments.add(segmentHit);
						}
					}
				}
			}			
		}
		boolean hit = !((hoveredNodes.isEmpty())&&(hoveredWays.isEmpty())&&(hoveredSegments.isEmpty()));
		preselectNode = hoveredNodes.size() - 1;
		preselectWay = hoveredWays.size() - 1;
		preselectSegment = hoveredSegments.size() - 1;
		
		//repaint if there is a hit or if the hit status changes
		if((hit)||(prevHit != hit)) {
			mapPanel.repaint();
		}
	}
	
	public void mousePressed(MouseEvent e) {
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc, mouseMerc);
		
		if(editMode == EditMode.SelectTool) {
			if(inMove) {
				if(!selection.isEmpty()) {
					//execute the move
					EditDestPoint dest = getDestinationPoint(mouseMerc);
					editManager.selectionMoved(selection,moveStartPoint,dest);
				}
			}
			else {
				//do a selection
				
				//store the latest point used for selection, for the move anchor
				moveStartPoint = new EditDestPoint();
				moveStartPoint.point = mouseMerc;
				
				if(!hoveredNodes.isEmpty()) {
					OsmNode node = hoveredNodes.get(preselectNode);
					if(!e.isShiftDown()) {
						selection.clear();
					}
					selection.add(node);
					
					//add a snap node for a move start
					moveStartPoint.snapNode = node;
				}
				else if(!hoveredWays.isEmpty()) {
					OsmWay way = hoveredWays.get(preselectWay);
					if(!e.isShiftDown()) {
						selection.clear();
					}
					selection.add(way);
					
					//no snap for move start for now with ways
				}
				else {
					if(!e.isShiftDown()) {
						selection.clear();
					}
				}
			}
		}
		else if(editMode == EditMode.NodeTool) {
			//execute a node addition
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			OsmNode node = editManager.nodeToolClicked(dest,featureInfo);
		}
		else if(editMode == EditMode.WayTool) {
			//execute a way node addition
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			activeWay = editManager.wayToolClicked(activeWay,isEnd,dest,featureInfo);
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
			else if(!hoveredSegments.isEmpty()) {
				preselectSegment--;
				if(preselectSegment < 0) preselectSegment = hoveredSegments.size() - 1;
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
			else if(!hoveredSegments.isEmpty()) {
				preselectSegment++;
				if(preselectSegment >= hoveredSegments.size()) preselectSegment = 0;
				changed = true;
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_M) {
			inMove = true;
		}
		
		if(changed) {
			getMapPanel().repaint();
//			java.awt.Toolkit.getDefaultToolkit().beep();
		}
    }

    /** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
		if(inMove) {
			inMove = false;
		}
    }
	
	//============================
	// Private Methods
	//============================
	
	/** This method returns the current edit destination point based on the
	 * currently selected hover node or segment. 
	 * 
	 * @param mouseMerc		The location of the mouse in mercator coordinates.
	 * @return				The EditDestPoint
	 */
	private EditDestPoint getDestinationPoint(Point2D mouseMerc) {
		EditDestPoint edp = new EditDestPoint();
		edp.point = mouseMerc;
		
		if(!hoveredNodes.isEmpty()) {
			edp.snapNode = hoveredNodes.get(preselectNode);
			edp.snapNode2 = null;
		}
		if(!hoveredSegments.isEmpty()) {
			NodeSegment ns = hoveredSegments.get(preselectSegment);
			edp.snapNode = ns.node1;
			edp.snapNode2 = ns.node2;
		}
		
		return edp;
	}
	
	//-----------------------
	// Hit Methods
	//-----------------------
	
	private void clearSelection() {
		selection.clear();
		activeWay = null;
		inMove = false;
		getMapPanel().repaint();
	}
	
	private void clearHover() {
		hoveredNodes.clear();
		hoveredWays.clear();
		hoveredSegments.clear();
		getMapPanel().repaint();
	}
	
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
	
	private NodeSegment getHitSegment(OsmWay way, Point2D mouseMerc, double mercRadSq) {
		OsmNode prevNode = null;
		for(OsmNode node:way.getNodes()) {
			if(prevNode != null) {
				if((segmentHit(node,prevNode,mouseMerc,mercRadSq))&&(getSegmentIsNew(node,prevNode))) {
					NodeSegment nodeSegment = new NodeSegment(prevNode,node);
					return nodeSegment;
				}
			}
			prevNode = node;
		}
		return null;
	}
	
	/** This method checks if the given pair of nodes is in the hover list already.
	 * If so, it returns false, if not it returns true. */
	private boolean getSegmentIsNew(OsmNode node1, OsmNode node2) {
		for(NodeSegment ns:hoveredSegments) {
			if(((ns.node1 == node1)&&(ns.node2 == node2))||((ns.node1 == node2)&&(ns.node2 == node1))) {
				return false;
			}
		}
		return true;
	}
	
	/** This returns true if a segment was hit. */
	private boolean segmentHit(OsmNode node1, OsmNode node2, Point2D mercPoint, double mercRadSq) {
		Point2D p1 = node1.getPoint();
		Point2D p2 = node2.getPoint();
		return Line2D.ptSegDistSq(p1.getX(),p1.getY(),p2.getX(),p2.getY(),mercPoint.getX(),mercPoint.getY()) < mercRadSq;
	}
	
	public class NodeSegment {
		public OsmNode node1;
		public OsmNode node2;
		
		public NodeSegment(OsmNode node1, OsmNode node2) {
			this.node1 = node1;
			this.node2 = node2;
		} 
	}
}
