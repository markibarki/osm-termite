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
import java.awt.MouseInfo;

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
	
	//this is the limit for ignoring pairs of lines for intersecting
	private final static double ALMOST_PARALLEL_SIN_THETA = .1; //5.7 degrees
	
	private final static double RADIUS_PIXELS = 5; 
	private final static float SELECT_WIDTH = 3;
	private final static float HOVER_PRESELECT_WIDTH = 2;
	private final static float HOVER_EXTENSION_WIDTH = 2;
	private final static float HOVER_OTHER_WIDTH = 2;
	private final static float PENDING_WIDTH = 1;
	private final static float MITER_LIMIT = 5f;
	
	private final static Color SELECT_COLOR = Color.RED;
	private final static Color HOVER_PRESELECT_COLOR = Color.MAGENTA;
	private final static Color HOVER_OTHER_COLOR = Color.PINK;
	private final static Color PENDING_COLOR = Color.BLACK;
	
	private final static float[] DASH_SPACING = {3f};
	private final static float DASH_PHASE = 0f;
	
	private final static BasicStroke SELECT_STROKE = new BasicStroke(SELECT_WIDTH);
	private final static BasicStroke HOVER_PRESELECT_STROKE = new BasicStroke(HOVER_PRESELECT_WIDTH);
	private final static BasicStroke HOVER_EXTENSION_STROKE = new BasicStroke(HOVER_EXTENSION_WIDTH, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, MITER_LIMIT,DASH_SPACING,DASH_PHASE);
	private final static BasicStroke HOVER_OTHER_STROKE = new BasicStroke(HOVER_OTHER_WIDTH);
	private final static BasicStroke PENDING_MOVE_STROKE = new BasicStroke(PENDING_WIDTH);
	private final static BasicStroke PENDING_CREATE_STROKE = new BasicStroke(PENDING_WIDTH, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, MITER_LIMIT,DASH_SPACING,DASH_PHASE);
	
	private OsmData osmData;
	private EditManager editManager;
	
	private EditMode editMode;
	private FeatureInfo featureInfo;
	private OsmWay activeStructure;
	private OsmRelation activeLevel;
	
	
	//these variables hold the hover state
	private OsmNode snapNode;
	private SnapSegment snapSegment;
	private SnapIntersection snapIntersection;
	private List<OsmWay> hoveredWays = new ArrayList<OsmWay>();
	private int preselectWay;
	private List<SnapSegment> hoveredSegments = new ArrayList<SnapSegment>();
	
	//this holds the active selection
	private List<OsmObject> selection = new ArrayList<OsmObject>();
	
	//There are nodes that are moving  with the mouse
	private List<EditNode> movingNodes = new ArrayList<EditNode>();
	//these are nodes that will be displayed with in the edit preview set
	//some of these are also moving nodes
	private List<EditNode> pendingNodes = new ArrayList<EditNode>();
	//these are segments that will be displayed with in the edit preview set
	//excluding ones that are checked for snapping. See pendingSnapSegments
	private List<EditSegment> pendingSegments = new ArrayList<EditSegment>();
	//these are pending nodes that also travel from a fixed node to a node tied to 
	//the mouse location - they are used to check some snap cases.
	private List<EditSegment> pendingSnapSegments = new ArrayList<EditSegment>();
	
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
			//initialize virtual node with a dummy point - it will get updated on a mouse move
			setNodeToolPendingData(new Point2D.Double());
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
			//initialize virtual node with a dummy point - it will get updated on a mouse move
			setWayToolPendingData(new Point2D.Double());
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
		if(snapNode != null) {
			g2.setColor(HOVER_PRESELECT_COLOR);
			//render
			renderPoint(g2,snapNode.getPoint(),mercatorToPixels,pixXY,rect);
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
		else if(snapIntersection != null) {
			g2.setColor(HOVER_PRESELECT_COLOR);
			SnapSegment ss;
			
			ss = snapIntersection.s1;
			if(ss.snapType == SnapType.SEGMENT_INT) {
				g2.setStroke(HOVER_PRESELECT_STROKE);
			}
			else {
				g2.setStroke(HOVER_EXTENSION_STROKE);
			}
			renderSegment(g2,ss.p1,ss.p2,mercatorToPixels,pixXY,prevPixXY,line);
			
			ss = snapIntersection.s2;
			if(ss.snapType == SnapType.SEGMENT_INT) {
				g2.setStroke(HOVER_PRESELECT_STROKE);
			}
			else {
				g2.setStroke(HOVER_EXTENSION_STROKE);
			}
			renderSegment(g2,ss.p1,ss.p2,mercatorToPixels,pixXY,prevPixXY,line);
		}
		else if(snapSegment != null) {
			g2.setColor(HOVER_PRESELECT_COLOR);
			if(snapSegment.snapType == SnapType.SEGMENT_INT) {
				g2.setStroke(HOVER_PRESELECT_STROKE);
			}
			else {
				g2.setStroke(HOVER_EXTENSION_STROKE);
			}
			renderSegment(g2,snapSegment.p1,snapSegment.p2,mercatorToPixels,pixXY,prevPixXY,line);
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
			for(EditSegment es:pendingSegments) {
				if(es.osmSegment != null) {
					g2.setStroke(PENDING_MOVE_STROKE);
				}
				else {
					g2.setStroke(PENDING_CREATE_STROKE);
				}
				renderSegment(g2,es.en1.point,es.en2.point,mercatorToPixels,pixXY,prevPixXY,line);
			}
		}
		if(!pendingSnapSegments.isEmpty()) {
			g2.setColor(PENDING_COLOR);
			for(EditSegment es:pendingSnapSegments) {
				if(es.osmSegment != null) {
					g2.setStroke(PENDING_MOVE_STROKE);
				}
				else {
					g2.setStroke(PENDING_CREATE_STROKE);
				}
				renderSegment(g2,es.en1.point,es.en2.point,mercatorToPixels,pixXY,prevPixXY,line);
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
		
		boolean wasActive = ((snapNode != null)||(snapSegment != null)||
				(snapIntersection != null)||(!hoveredWays.isEmpty())||(!movingNodes.isEmpty()));

		//clear snapObjects
		snapNode = null;
		snapSegment = null;
		snapIntersection = null;
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
		double minNodeErr2 = mercRadSq;
		GraduatedList<OsmObject> objectList = osmData.getOrderedList();
		for(java.util.List<OsmObject> subList:objectList.getLists()) {
			for(OsmObject mapObject:subList) {
				//make sure edit is enabled for this object
				if(!mapObject.editEnabled()) continue;
				
				//check for hit
				if(mapObject instanceof OsmNode) {
					//check for a node hit
					double err2 = mouseMerc.distanceSq(((OsmNode)mapObject).getPoint());
					if(err2 < minNodeErr2) {
						snapNode = (OsmNode)mapObject;
					}
					//check for a segment hit
					for(OsmSegment segment:((OsmNode)mapObject).getSegments()) {
						//only do the segments that start with this node, to avoid doing them twice
						if(segment.getNode1() == mapObject) {
							if((editMode == EditMode.SelectTool)&&(!inMove)) {
								//selection preview
								//check for segment hit
								if(segmentHit(segment,mouseMerc,mercRadSq)) {
									//add ways for this segment
									for(OsmWay way:segment.getOsmWays()) {
										hoveredWays.add(way);
									}
								}
							}
							else {
								//snap preview
								//check for segment and extension hit
								Point2D sp1 = segment.getNode1().getPoint();
								Point2D sp2 = segment.getNode2().getPoint();
								SnapSegment ss = getSnapSegment(mouseMerc,sp1,sp2,mercRadSq);
								if(ss != null) {
									hoveredSegments.add(ss);
								}
							}		
						}
						
					}
				}
			}			
		}
		
		//handle a move preview
		if(!movingNodes.isEmpty()) {
			updateMovingNodes(mouseMerc);
		}
		
		//check for snapping in the pending snap segments
		SnapSegment ss;
		for(EditSegment es:pendingSnapSegments) {
			//check for horizontal snap
			ss = getHorOrVertSnapSegment(es,mouseMerc,mercRadSq);
			if(ss != null) {
				hoveredSegments.add(ss);
			}
			else {
				//only check perpicular if it is not already a horizontal or vertical snap
				//check for perps from both ends
				ss = getPerpSegment(es,mouseMerc,mercRadSq);
				if(ss != null) {
					hoveredSegments.add(ss);
				}
			}
		}
		
		//select the segment and intersection
		if(!hoveredSegments.isEmpty()) {
			selectActiveSegment(mouseMerc,mercRadSq);
		}
		
		//select the active object, this can be updated with the arrow keys
		//as it is now, it resets every move. we might want to change this.
		preselectWay = hoveredWays.size() - 1;
		
		boolean isActive = ((snapNode != null)||(snapSegment != null)||
				(snapIntersection != null)||(!hoveredWays.isEmpty())||(!movingNodes.isEmpty()));
		
		//repaint if there is a hit or if the hit status changes
		if(wasActive||isActive) {
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
				if((!selection.isEmpty())&&(e.getButton() == MouseEvent.BUTTON1)) {
					//execute the move
					EditDestPoint dest = getDestinationPoint(mouseMerc);
					editManager.selectionMoved(selection,moveStartPoint,dest);
					clearPending();
					
					//update the start point location - just change the point
					moveStartPoint.point.setLocation(mouseMerc);
				}
			}
			else {
				//do a selection
				
				//store the latest point used for selection, for the move anchor
				moveStartPoint = new EditDestPoint();
				moveStartPoint.point = new Point2D.Double(mouseMerc.getX(),mouseMerc.getY());
				
				OsmObject obj = null;
				if(snapNode != null) {
					obj = snapNode;
					
					//add a snap node for a move start
					moveStartPoint.snapNode = snapNode;
					moveStartPoint.point.setLocation(snapNode.getPoint());
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
			//prepare for next
			setNodeToolPendingData(mouseMerc);
		}
		else if(editMode == EditMode.WayTool) {
			//execute a way node addition
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			activeWay = editManager.wayToolClicked(activeWay,isEnd,dest,featureInfo,activeLevel);
			//prepare for next
			setWayToolPendingData(mouseMerc);
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
			if(!hoveredWays.isEmpty()) {
				preselectWay--;
				if(preselectWay < 0) preselectWay = hoveredWays.size() - 1;
				changed = true;
			}
		}
		else if((e.getKeyCode() == KeyEvent.VK_RIGHT)||(e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(!hoveredWays.isEmpty()) {
				preselectWay++;
				if(preselectWay >= hoveredWays.size()) preselectWay = 0;
				changed = true;
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_M) {
			if(!inMove) {
				inMove = true;
				loadPendingFromSelection();

				//get the current mouse location and update the nodes that move with the mouse
				MapPanel mapPanel = getMapPanel();
				AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
				java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
				java.awt.Point mapPanelInApp = mapPanel.getLocationOnScreen();
				Point2D mousePix = new Point2D.Double(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
				Point2D mouseMerc = new Point2D.Double(); 
				pixelsToMercator.transform(mousePix,mouseMerc);
				updateMovingNodes(mouseMerc);

				changed = true;
			}
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
			getMapPanel().repaint();
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
		edp.point = new Point2D.Double();
		edp.snapNode = null;
		
		if(snapNode != null) {
			edp.snapNode = snapNode;
			edp.point.setLocation(edp.snapNode.getPoint());
		}
		else if(snapIntersection != null) {
			edp.point = snapIntersection.ps;
		}
		else if(snapSegment != null) {
			edp.point = snapSegment.ps;
		}
		else {
			edp.point.setLocation(mouseMerc);
		}
		
		return edp;
	}
	
	private SnapSegment getSnapSegment(Point2D inPoint, Point2D segPt1, Point2D segPt2, double mercRadSq) {
		//check for a hit
		if(Line2D.ptLineDistSq(segPt1.getX(),segPt1.getY(),segPt2.getX(),segPt2.getY(),inPoint.getX(),inPoint.getY()) >= mercRadSq) {
			//no hit for this segment
			return null;
		}
			
		//calculate the hit point
		SnapSegment ss = new SnapSegment();
		double dxs = segPt2.getX() - segPt1.getX();
		double dys = segPt2.getY() - segPt1.getY();
		double dxp = inPoint.getX() - segPt1.getX();
		double dyp = inPoint.getY() - segPt1.getY();
		
		double fraction = (dxs * dxp + dys * dyp)/(dxs * dxs + dys * dys);
		ss.ps = new Point2D.Double(segPt1.getX() + fraction * dxs,segPt1.getY() + fraction * dys);
		ss.err2 = ss.ps.distanceSq(inPoint);
		if(fraction < 0) {
			//snap to extension from point 1
			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
			ss.p2 = ss.ps;
			ss.snapType = SnapType.SEGMENT_EXT;
		}
		else if(fraction > 1) {
			//snap to extension from point 2
			ss.p1 = new Point2D.Double(segPt2.getX(),segPt2.getY());
			ss.p2 = ss.ps;
			ss.snapType = SnapType.SEGMENT_EXT;
		}
		else {
			//snap to segment
			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
			ss.p2 = new Point2D.Double(segPt2.getX(),segPt2.getY());
			ss.snapType = SnapType.SEGMENT_INT;
		}
		
		return ss;
	}
	
	/** This method selects the active snap segments from the list of hovered
	 * segments. It discards the non-active segments. */
	private void selectActiveSegment(Point2D mouseMerc, double mercRadSq) {
		//least error segment
		SnapSegment ss0 = null;
		double err2 = Double.MAX_VALUE;
		//intersection 
		SnapIntersection si0 = null;
		//working values
		SnapSegment ss1;
		SnapSegment ss2;
		int cnt = hoveredSegments.size();
		for(int i = 0; i < cnt; i++) {
			ss1 = hoveredSegments.get(i);
			//get least error
			if(ss1.err2 < err2) {
				ss0 = ss1;
			}
			//look for intersection
			for(int j = i+1; j < cnt; j++) {
				ss2 = hoveredSegments.get(j);
				SnapIntersection si = checkIntersection(ss1,ss2,mouseMerc,mercRadSq);
				//save best
				if((si != null)&&((si0 == null)||(si.err2 < si0.err2))) {
					si0 = si;
				}
			}
		}
		//sve results
		snapSegment = ss0;
		snapIntersection = si0;
	}
	
	private SnapIntersection checkIntersection(SnapSegment ss1, SnapSegment ss2, 
			Point2D mouseMer, double mercRadSq) {
		
		double xs1 = ss1.p1.getX();
		double ys1 = ss1.p1.getY();
		double dx1 = ss1.p2.getX() - xs1;
		double dy1 = ss1.p2.getY() - ys1;
		double xs2 = ss2.p1.getX();
		double ys2 = ss2.p1.getY();
		double dx2 = ss2.p2.getX() - xs2;
		double dy2 = ss2.p2.getY() - ys2;
		
		double den = dx1*dy2 - dy1*dx2;
		double len1 = Math.sqrt(dx1*dx1 + dy1*dy1);
		double len2 = Math.sqrt(dx2*dx2 + dy2*dy2);
		
		//make sure lines are not cloe to being parallel
		if( Math.abs(den / (len1 * len2)) < ALMOST_PARALLEL_SIN_THETA) return null;
		
		//find intersection
		double num = -(xs1*dy2 - ys1*dx2) + (xs2*dy2 - ys2*dx2);
		double alpha = num / den;
		
		double xSnap = xs1 + alpha * dx1;
		double ySnap = ys1 + alpha * dy1;
		
		double err2 = mouseMer.distanceSq(xSnap,ySnap);
		
		if(err2 >= mercRadSq) {
			//not in range
			return null;	
		}	
		
		SnapIntersection si = new SnapIntersection();
		si.ps = new Point2D.Double(xSnap,ySnap);
		si.s1 = ss1;
		si.s2 = ss2;
		si.err2 = err2;
		return si;
		
	}
	
	/** This method creates the best horizontal or vertical snap for the given segment and mouse point.
	 * The segment should include the mouse point as one of the ends. If no perpendicular snap is
	 * found null is returned. */
	private SnapSegment getHorOrVertSnapSegment(EditSegment es, Point2D mouseMerc, double mercRadSq) {
		double dx1 = Math.abs(mouseMerc.getX() - es.en1.point.getX());
		double dy1 = Math.abs(mouseMerc.getY() - es.en1.point.getY());
		double dx2 = Math.abs(mouseMerc.getX() - es.en2.point.getX());
		double dy2 = Math.abs(mouseMerc.getY() - es.en2.point.getY());
		double err2;
		SnapSegment ss = null;
		if((dx1 == 0)&&(dy1 == 0)) {
			if(dx2 < dy2) {
				err2 = dx2 * dx2;
				if(err2 < mercRadSq) {
					ss = new SnapSegment();
					ss.p1 = es.en2.point;
					ss.p2 = new Point2D.Double(es.en2.point.getX(),mouseMerc.getY());
					ss.ps = ss.p2;
					ss.err2 = err2;
					ss.snapType = SnapType.HORIZONTAL;
				}
			}
			else {
				err2 = dy2 * dy2;
				if(err2 < mercRadSq) {
					ss = new SnapSegment();
					ss.p1 = es.en2.point;
					ss.p2 = new Point2D.Double(mouseMerc.getX(),es.en2.point.getY());
					ss.ps = ss.p2;
					ss.err2 = err2;
					ss.snapType = SnapType.VERTICAL;
				}
			}
		}
		else if((dx2 == 0)&&(dy2 == 0)) {
			if(dx1 < dy1) {
				err2 = dx1 * dx1;
				if(err2 < mercRadSq) {
					ss = new SnapSegment();
					ss.p1 = es.en1.point;
					ss.p2 = new Point2D.Double(es.en1.point.getX(),mouseMerc.getY());
					ss.ps = ss.p2;
					ss.err2 = err2;
					ss.snapType = SnapType.HORIZONTAL;
				}
			}
			else {
				err2 = dy1 * dy1;
				if(err2 < mercRadSq) {
					ss = new SnapSegment();
					ss.p1 = es.en1.point;
					ss.p2 = new Point2D.Double(mouseMerc.getX(),es.en1.point.getY());
					ss.ps = ss.p2;
					ss.err2 = err2;
					ss.snapType = SnapType.VERTICAL;
				}
			}
		}
		return ss;
	}
	
	/** This method loads the best perpindicualr snap segment. If none is found
	 * null is returned. */
	private SnapSegment getPerpSegment(EditSegment editSegment, Point2D mouseMerc, double mercRadSq) {
		OsmNode pivotNode;
		Point2D pivotPoint = new Point2D.Double();
		SnapSegment ss;
		SnapSegment ss0 = null;
		OsmNode node;
		Point2D basePoint;
		
		//check for perpindiculars at node 1
		node = editSegment.en1.node;
		if(node != null) {
			basePoint = node.getPoint();
			for(OsmSegment segment:node.getSegments()) {
				if(segment.getNode1() == node) {
					pivotNode = segment.getNode2();
				}
				else {
					pivotNode = segment.getNode1();
				}
				double dx = pivotNode.getPoint().getX() - basePoint.getX();
				double dy = pivotNode.getPoint().getY() - basePoint.getY();
				pivotPoint.setLocation(basePoint.getX() - dy,basePoint.getY() + dx);
				ss = this.getSnapSegment(mouseMerc,basePoint,pivotPoint,mercRadSq);
				if((ss != null)&&((ss0 == null)||(ss.err2 < ss0.err2))) {
					ss0 = ss;
				}
			}
		}
		
		//check for perpindiculars at node 2
		node = editSegment.en2.node;
		if(node != null) {
			basePoint = node.getPoint();
			for(OsmSegment segment:node.getSegments()) {
				if(segment.getNode1() == node) {
					pivotNode = segment.getNode2();
				}
				else {
					pivotNode = segment.getNode1();
				}
				double dx = pivotNode.getPoint().getX() - basePoint.getX();
				double dy = pivotNode.getPoint().getY() - basePoint.getY();
				pivotPoint.setLocation(basePoint.getX() - dy,basePoint.getY() + dx);
				ss = this.getSnapSegment(mouseMerc,basePoint,pivotPoint,mercRadSq);
				if((ss != null)&&((ss0 == null)||(ss.err2 < ss0.err2))) {
					ss0 = ss;
				}
			}
		}
		
		//we calculated ss0 as if it were based on a real segment
		//it should be based on a perpindicular virtual segment - correct it
		//cludge allert
		if(ss0 != null) {
			ss0.snapType = SnapType.SEGMENT_PERP;
			//one end of the segment should be the mouse
			// we want to draw from the other end to the snap point
			if((mouseMerc.getX() == editSegment.en1.point.getX())&&
					(mouseMerc.getY() == editSegment.en1.point.getY())) {
				ss0.p1 = editSegment.en2.point;
			}
			else {
				ss0.p1 = editSegment.en1.point;
			}
			ss0.p2 = ss0.ps;	
		}
		
		return ss0;
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
		snapNode = null;
		snapSegment = null;
		snapIntersection = null;
		hoveredWays.clear();
		hoveredSegments.clear();
		getMapPanel().repaint();
	}
	
	/** This returns true if a segment was hit. */
	private boolean segmentHit(OsmSegment segment, Point2D mercPoint, double mercRadSq) {
		Point2D p1 = segment.getNode1().getPoint();
		Point2D p2 = segment.getNode2().getPoint();
		return Line2D.ptSegDistSq(p1.getX(),p1.getY(),p2.getX(),p2.getY(),mercPoint.getX(),mercPoint.getY()) < mercRadSq;
	}
	
	// </editor-fold>
	
	private void updateMovingNodes(Point2D mouseMerc) {
		if(inMove) {
			//this is a move - there may be several nodes
			if(moveStartPoint != null) {
				double dx = mouseMerc.getX() - moveStartPoint.point.getX();
				double dy = mouseMerc.getY() - moveStartPoint.point.getY();
				for(EditNode en:movingNodes) {
					Point2D nodePoint = en.node.getPoint();
					en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
				}
			}
		}
		else {
			//this is a new node - there should be just one node
			if(!movingNodes.isEmpty()) {
				EditNode en = movingNodes.get(0);
				en.point.setLocation(mouseMerc);
			}
		}
	}
	
	// <editor-fold defaultstate="collapsed" desc="Pending Methods">
	private void clearPending() {
		pendingNodes.clear();
		pendingSegments.clear();
		pendingSnapSegments.clear();
		movingNodes.clear();
	}
	
	private void loadPendingFromSelection() {
		//get unique copies of nodes
		HashMap<OsmNode,EditNode> nodeMap = new HashMap<OsmNode,EditNode>();
		for(OsmObject obj:selection) {
			if(obj instanceof OsmNode) {
				addNodeToMap(nodeMap,(OsmNode)obj);
			}
			else if(obj instanceof OsmWay) {
				for(OsmNode node:((OsmWay)obj).getNodes()) {
					addNodeToMap(nodeMap,node);
				}
			}
		}

		//load the edit segments - we may have duplicates here but that is ok
		for(OsmNode node:nodeMap.keySet()) {
			for(OsmSegment segment:node.getSegments()) {
				boolean segmentHashNonMovingNode = false;
				EditNode en1 = nodeMap.get(segment.getNode1());
				if(en1 == null) {
					en1 = new EditNode(segment.getNode1());
					segmentHashNonMovingNode = true;
				}
				EditNode en2 = nodeMap.get(segment.getNode2());
				if(en2 == null) {
					en2 = new EditNode(segment.getNode2());
					segmentHashNonMovingNode = true;
				}
				EditSegment es = new EditSegment(en1,en2,segment);
				//add this to pendingSnapSegments only if it connects to the node
				//that is tied to the mouse location and the other node is not
				//a moving node
				if((node == moveStartPoint.snapNode)&&(segmentHashNonMovingNode)) {
					pendingSnapSegments.add(es);
				}
				else {
					pendingSegments.add(es);
				}
			}
		}
	}

	private void addNodeToMap(HashMap<OsmNode,EditNode> nodeMap,OsmNode node) {
		if(!nodeMap.containsKey(node)) {
			EditNode en = new EditNode(node);
			pendingNodes.add(en);
			movingNodes.add(en);
			nodeMap.put(node,en);
		}
	}
	
	private void setNodeToolPendingData(Point2D mouseMerc) {
		clearPending();
		
		EditNode en = new EditNode(mouseMerc,featureInfo);
		movingNodes.add(en);
		pendingNodes.add(en);
	}
	
	private void setWayToolPendingData(Point2D mouseMerc) {
		clearPending();
		
		//get the node to add
		EditNode en = new EditNode(mouseMerc,null);
		movingNodes.add(en);
		pendingNodes.add(en);
		//get the segment from the previous node
		OsmNode activeNode = null;
		if(activeWay != null) {
			List<OsmNode> nodes = activeWay.getNodes();
			
			if(!nodes.isEmpty()) {
				if(isEnd) {
					activeNode = nodes.get(nodes.size()-1);
				}
				else {
					activeNode = nodes.get(0);
				}
			}
		}
		if(activeNode != null) {
			EditNode en2 = new EditNode(activeNode);
			EditSegment es = new EditSegment(en,en2,null);
			pendingNodes.add(en2);
			pendingSnapSegments.add(es);
		}
		
	}
	// </editor-fold>

	private enum SnapType {
		SEGMENT_INT,
		SEGMENT_EXT,
		SEGMENT_PERP,
		INTERSECTION,
		HORIZONTAL,
		VERTICAL
	}
	
	private class SnapSegment {
		//display line start
		public Point2D p1;
		//display line end
		public Point2D p2;
		//target point
		public Point2D ps;
		//type
		public SnapType snapType;
		//error
		public double err2;
	}
	
	private class SnapIntersection {
		public Point2D ps;
		public SnapSegment s1;
		public SnapSegment s2;
		public double err2;
	}
}
