package intransix.osm.termite.render.edit;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.*;
import java.awt.event.*;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.util.GraduatedList;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.gui.*;
import intransix.osm.termite.gui.stdmode.*;

/**
 * This layer controls the user interaction with the active map data. It is designed
 * to run with the editor modes for the Select Tool, Node Tool and Way Tool.
 * 
 * @author sutter
 */
public class EditLayer extends MapLayer implements MapDataListener, 
		FeatureLayerListener, LevelSelectedListener,  
		MouseListener, MouseMotionListener, KeyListener, FocusListener {
	
	//=========================
	// Properties 
	//=========================
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	private enum EditMode {
		SelectTool,
		NodeTool,
		WayTool,
		Other
	}
	
	private final static double RADIUS_PIXELS = 5; 
	private final static float SELECT_WIDTH = 3;
	private final static float HOVER_PRESELECT_WIDTH = 4;
	private final static float HOVER_OTHER_WIDTH = 2;
	private final static float PENDING_WIDTH = 2;
	
	private final static Color SELECT_COLOR = Color.RED;
	private final static Color HOVER_PRESELECT_COLOR = Color.MAGENTA;
	private final static Color HOVER_OTHER_COLOR = Color.PINK;
	private final static Color PENDING_COLOR = Color.BLACK;
	
	private final static BasicStroke SELECT_STROKE = new BasicStroke(SELECT_WIDTH);
	private final static BasicStroke HOVER_PRESELECT_STROKE = new BasicStroke(HOVER_PRESELECT_WIDTH);
	private final static BasicStroke HOVER_OTHER_STROKE = new BasicStroke(HOVER_OTHER_WIDTH);
	private final static BasicStroke PENDING_STROKE = new BasicStroke(PENDING_WIDTH);
	
	private OsmData osmData;
	private EditManager editManager;
	
	private EditMode editMode;
	private FeatureInfo featureInfo;
	private OsmWay activeStructure;
	private OsmRelation activeLevel;
	
	
	//these variables hold the hover state
	private List<OsmNode> hoveredNodes = new ArrayList<OsmNode>();
	private int preselectNode;
	private List<OsmWay> hoveredWays = new ArrayList<OsmWay>();
	private int preselectWay;
	private List<NodeSegment> hoveredSegments = new ArrayList<NodeSegment>();
	private int preselectSegment;
	
	//this holds the active selection
	private List<OsmObject> selection = new ArrayList<OsmObject>();
	
	//these variables hold the pending edit state
	private List<EditNode> movingNodes = new ArrayList<EditNode>();
	private List<EditNode> pendingNodes = new ArrayList<EditNode>();
	private List<EditSegment> pendingSegments = new ArrayList<EditSegment>();
	
	//working variables
	private EditDestPoint moveStartPoint;
	private boolean inMove;
	private OsmWay activeWay;
	private boolean isEnd;
	
	// </editor-fold>
	
	//=========================
	// Public Methods
	//=========================
	
	/** This returns the current selection. */
	public List<OsmObject> getSelection() {
		return selection;
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
				//if there is a single way selected, use it as the active way
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
	
	// <editor-fold defaultstate="collapsed" desc="Render">
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
				renderPoint(g2,node.getPoint(),mercatorToPixels,pixXY,rect);
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
				renderSegment(g2,ns.node1.getPoint(),ns.node2.getPoint(),
						mercatorToPixels,pixXY,prevPixXY,line);
			}
		}
		
		//render pending objects
		if(!pendingNodes.isEmpty()) {
			g2.setColor(PENDING_COLOR);
			for(EditNode en:pendingNodes) {
				renderPoint(g2,en.point,mercatorToPixels,pixXY,rect);
			}
		}
		if(!pendingSegments.isEmpty()) {
			g2.setColor(PENDING_COLOR);
			g2.setStroke(PENDING_STROKE);
			for(EditSegment es:pendingSegments) {
				renderSegment(g2,es.p1,es.p2,mercatorToPixels,pixXY,prevPixXY,line);
			}
		}
		
		//render selection
		if(!selection.isEmpty()) {
			g2.setColor(SELECT_COLOR);
			g2.setStroke(SELECT_STROKE);
			for(OsmObject osmObject:selection) {
				if(osmObject instanceof OsmNode) {
					renderPoint(g2,((OsmNode)osmObject).getPoint(),mercatorToPixels,pixXY,rect);
				}
				else if(osmObject instanceof OsmWay) {
					renderWay(g2,(OsmWay)osmObject,mercatorToPixels,pixXY,prevPixXY,line);
				}
			}
		}
	}
	
	private void renderPoint(Graphics2D g2, Point2D point, 
			AffineTransform mercatorToPixels, Point2D pixXY, Rectangle2D rect) {
		mercatorToPixels.transform(point,pixXY);
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
	
	private void renderSegment(Graphics2D g2, Point2D p1, Point2D p2, 
			AffineTransform mercatorToPixels, Point2D pixXY, Point2D prevPixXY, Line2D line) {
		
		mercatorToPixels.transform(p1,prevPixXY);
		mercatorToPixels.transform(p2,pixXY);
		line.setLine(pixXY,prevPixXY);
		g2.draw(line);
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Map Data Listener">
	/** This method is called when the map data is set. */
	@Override
	public void onMapData(OsmData osmData) {
		this.osmData = osmData;
		if(osmData != null) {
			editManager = new EditManager(osmData);
		}
		else {
			editManager = null;
			activeStructure = null;
			activeLevel = null;
		}
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Feature Layer Listener">
	/** This method is called when a feature layer is selected. It may be called 
	 * with the value null if a selection is cleared an no new selection is made. 
	 * 
	 * @param featureInfo	The selected feature type
	 */
	@Override
	public void onFeatureLayerSelected(FeatureInfo featureInfo) {
		this.featureInfo = featureInfo;
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Level Selected Listener">
	/** This method is called when a map level is selected. It may be called 
	 * with the value null for the level or the level and the structure. 
	 * 
	 * @param structure		The footprint in the outdoor map for the selected level
	 * @param level			The selected level
	 */
	@Override
	public void onLevelSelected(OsmWay structure, OsmRelation level) {
		this.activeStructure = structure;
		this.activeLevel = level;
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Mouse Listeners">
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		this.getMapPanel().requestFocusInWindow();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		clearHover();
	}
	
	@Override
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
		
		//handle a move
		boolean moved = false;
		if(!movingNodes.isEmpty()) {
			if(moveStartPoint != null) {
				double dx = mouseMerc.getX() - moveStartPoint.point.getX();
				double dy = mouseMerc.getY() - moveStartPoint.point.getY();
				for(EditNode en:movingNodes) {
					Point2D nodePoint = en.node.getPoint();
					en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
				}
				moved = true;
			}
		}
		
		//repaint if there is a hit or if the hit status changes
		if((hit)||(prevHit != hit)||(moved)) {
			mapPanel.repaint();
		}
	}
	
	@Override
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
				
				OsmObject obj = null;
				if(!hoveredNodes.isEmpty()) {
					obj = hoveredNodes.get(preselectNode);
					
					//add a snap node for a move start
					moveStartPoint.snapNode = (OsmNode)obj;
				}
				else if(!hoveredWays.isEmpty()) {
					obj = hoveredWays.get(preselectWay);
					//no snap for move start for now with ways
				}
				else {
					if(!e.isShiftDown()) {
						selection.clear();
					}
				}
				
				//handle selection
				if(!e.isShiftDown()) {
					//no shift - replace selection
					selection.clear();
					if(obj != null) {
						selection.add(obj);
					}
				}
				else {
					//shift - add or remove object
					if(obj != null) {
						if(selection.contains(obj)) {
							selection.remove(obj);
						}
						else {
							selection.add(obj);
						}
					}
				}
			}
		}
		else if(editMode == EditMode.NodeTool) {
			//execute a node addition
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			OsmNode node = editManager.nodeToolClicked(dest,featureInfo,activeLevel);
		}
		else if(editMode == EditMode.WayTool) {
			//execute a way node addition
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			activeWay = editManager.wayToolClicked(activeWay,isEnd,dest,featureInfo,activeLevel);
		}
		getMapPanel().repaint();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Key Listener and Focus Listener">
	
	/** Handle the key typed event from the text field. */
    @Override
	public void keyTyped(KeyEvent e) {
    }

    /** Handle the key-pressed event from the text field. */
	@Override
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
			loadPendingFromSelection();
			changed = true;
		}
		
		if(changed) {
			getMapPanel().repaint();
//			java.awt.Toolkit.getDefaultToolkit().beep();
		}
    }

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
		if(inMove) {
			inMove = false;
			clearPending();
		}
    }
	
	@Override
	public void focusGained(FocusEvent e) {
		
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		inMove = false;
		clearPending();
	}
	
	// </editor-fold>
	
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
	
	// <editor-fold defaultstate="collapsed" desc="Hit Methods">
	
	/** Thie method clears the current selection. */
	private void clearSelection() {
		selection.clear();
		activeWay = null;
		inMove = false;
		clearPending();
		getMapPanel().repaint();
	}
	
	/** This method clears the hover variables. */
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
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Pending Methods">
	private void clearPending() {
		pendingNodes.clear();
		pendingSegments.clear();
		movingNodes.clear();
	}
	
	private void loadPendingFromSelection() {
		for(OsmObject obj:selection) {
			if(obj instanceof OsmNode) {
				EditNode node = new EditNode((OsmNode)obj);
				pendingNodes.add(node);
				movingNodes.add(node);
			}
			else if(obj instanceof OsmWay) {
				EditSegment seg;
				OsmNode prev = null;
				for(OsmNode node:((OsmWay)obj).getNodes()) {
					if(prev != null) {
						seg = new EditSegment(node.getPoint(),prev.getPoint());
						pendingSegments.add(seg);
					}
					prev = node;
				}
			}
		}
	}
	// </editor-fold>
	public class NodeSegment {
		public OsmNode node1;
		public OsmNode node2;
		
		public NodeSegment(OsmNode node1, OsmNode node2) {
			this.node1 = node1;
			this.node2 = node2;
		} 
	}
}
