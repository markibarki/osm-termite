package intransix.osm.termite.render.source;

import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmSegment;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import intransix.osm.termite.render.*;
import intransix.osm.termite.render.edit.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SourceLayer extends MapLayer implements ImageObserver,
		MouseListener, MouseMotionListener, KeyListener, FocusListener {
	
	public enum LayerState {
		STATIC,
		PLACE_P1,
		PLACE_P2,
		SELECT
	}
	
	private final static int RADIUS_PIX = 10;
	private final static Color P1_COLOR = Color.BLUE;
	private final static Color P2_COLOR = Color.PINK;
	private final static Color SELECT_COLOR = Color.RED;
	private final static Color PREVIEW_COLOR = Color.BLACK;
	
	private LayerState layerState = LayerState.STATIC;
	private boolean selectionActive = false;
	private boolean selectionP1 = false;
	private boolean inMove = false;
	
	private Point2D p1Image = new Point2D.Double();
	private Point2D p1Merc;
	private Point2D p1Pix = new Point2D.Double();
	private Point2D p2Image = new Point2D.Double();
	private Point2D p2Merc;
	private Point2D p2Pix = new Point2D.Double();
	
	private Point2D moveMerc = new Point2D.Double();
	private Point2D movePix = new Point2D.Double();
	
	private File imageFile;
	private AffineTransform imageToMerc;
	private AffineTransform moveImageToMerc = new AffineTransform();
	private Image sourceImage;
	
	private AffineTransform workingTransform = new AffineTransform();
	
	public boolean loadImage(File file) {
		//reset the transform - we need to add a way to sace this
		imageToMerc = null;
		
		try {
			sourceImage = java.awt.Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
			return true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/** This mode sets the edit layer active. */
	@Override
	public void setActiveState(boolean isActive) {
		super.setActiveState(isActive);
		MapPanel mapPanel = this.getMapPanel();
		if(mapPanel != null) {
			if(isActive) {
				mapPanel.addMouseListener(this);
				mapPanel.addMouseMotionListener(this);
				mapPanel.addKeyListener(this);
				mapPanel.addFocusListener(this);
			}
			else {
				mapPanel.removeMouseListener(this);
				mapPanel.removeMouseMotionListener(this);
				mapPanel.removeKeyListener(this);
				mapPanel.removeFocusListener(this);
			}
		}
	}
	
	public void setLayerState(LayerState layerState) {
		this.layerState = layerState;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform base = g2.getTransform();
		
		MapPanel mapPanel = getMapPanel();
		AffineTransform mercToPixels = mapPanel.getMercatorToPixels();
		
		if(imageToMerc == null) {
			AffineTransform pixelsToMerc = mapPanel.getPixelsToMercator();
			Rectangle visibleRect = mapPanel.getVisibleRect();
			imageToMerc = new AffineTransform(pixelsToMerc);
		}
		
		//set the opacity for the layer
		Composite originalComposite = null;
		Composite activeComposite = this.getComposite();
		if(activeComposite != null) {
			originalComposite = g2.getComposite();
			g2.setComposite(activeComposite);
		}
		
		//transform to tile coordinates
		g2.transform(mercToPixels);
		
		AffineTransform activeTransform = inMove ? moveImageToMerc : imageToMerc;
		g2.transform(activeTransform);
		
		if(sourceImage != null) {
			g2.drawImage(sourceImage, 0, 0, this);
		}
		
		// return to base opacity and base transform
		if(originalComposite != null) {
			g2.setComposite(originalComposite);
		}
		g2.setTransform(base);
		
		//draw the points
		Point2D point;
		if(layerState != LayerState.STATIC) {
			if(p1Merc != null) {
				mercToPixels.transform(p1Merc, p1Pix);
				if((selectionActive)&&(selectionP1)) {
					if(inMove) {
						g2.setColor(PREVIEW_COLOR);
						point = movePix;
					}
					else {
						g2.setColor(SELECT_COLOR);
						point = p1Pix;
					}
				}
				else {
					g2.setColor(P1_COLOR);
					point = p1Pix;
				}
				renderPoint(g2,point);
			}
			if(p2Merc != null) {
				mercToPixels.transform(p2Merc, p2Pix);
				if((selectionActive)&&(!selectionP1)) {
					if(inMove) {
						g2.setColor(PREVIEW_COLOR);
						point = movePix;
					}
					else {
						g2.setColor(SELECT_COLOR);
						point = p2Pix;
					}
				}
				else {
					g2.setColor(P2_COLOR);
					point = p2Pix;
				}
				renderPoint(g2,point);
			}
		}
	}
	
	@Override
	public boolean imageUpdate(Image image, int infoflags, int x, int y, int width, int height) {
		if((infoflags & ImageObserver.ALLBITS) != 0) {
			//just do a repaint
			getMapPanel().repaint();
			return false;
		}
		else {
			return true;
		}
	}
	
		
	// <editor-fold defaultstate="collapsed" desc="Mouse Listeners">
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		//no edit move with mouse drag - explicit move command needed
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		this.getMapPanel().requestFocusInWindow();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		//read mouse location in global coordinates
		if(inMove) {
			MapPanel mapPanel = getMapPanel();
			AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
			movePix.setLocation(e.getX(),e.getY());
			pixelsToMercator.transform(movePix, moveMerc);
			updateMoveTransform();
			
			mapPanel.repaint();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		if(layerState == LayerState.STATIC) return;
	
		boolean changed = false;
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
		double mercRadius = RADIUS_PIX / mapPanel.getZoomScalePixelsPerMerc();
		
		Point2D mouseMerc = new Point2D.Double();
		mouseMerc.setLocation(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc,mouseMerc);
		
		if(layerState == LayerState.PLACE_P1) {
			//get inverse transform
			AffineTransform mercToImage;
			try {
				mercToImage = imageToMerc.createInverse();
			}
			catch(Exception ex){
				//should not happen
				ex.printStackTrace();
				return;
			}
			
			p1Merc = mouseMerc;
			mercToImage.transform(p1Merc,p1Image);
			p1Pix.setLocation(e.getX(),e.getY());
			changed = true;
		}
		else if(layerState == LayerState.PLACE_P2) {
			//get inverse transform
			AffineTransform mercToImage;
			try {
				mercToImage = imageToMerc.createInverse();
			}
			catch(Exception ex){
				//should not happen
				ex.printStackTrace();
				return;
			}
			
			p2Merc = mouseMerc;
			mercToImage.transform(p2Merc,p2Image);
			p1Pix.setLocation(e.getX(),e.getY());
			changed = true;
		}
		else if(layerState == LayerState.SELECT) {
			if((inMove)&&(selectionActive)) {
				
				movePix.setLocation(e.getX(),e.getY());
				moveMerc.setLocation(mouseMerc);
				updateMoveTransform();
				completeMove();
				changed = true;
				
				//clear selection
				selectionActive = false;
			}
			else {
				selectionActive = false;
				
				double distMerc;
				if(p1Merc != null) {
					distMerc = p1Merc.distance(mouseMerc);
					if(distMerc < mercRadius) {
						selectionActive = true;
						selectionP1 = true;
					}
				}
				if((p2Merc != null)&&(!selectionActive)) {
					distMerc = p2Merc.distance(mouseMerc);
					if(distMerc < mercRadius) {
						selectionActive = true;
						selectionP1 = false;
					}
				}
				
				changed = true;
			}
		}
		
		if(changed) {
			mapPanel.repaint();
		}
		
	
//		
//		if(e.getButton() == MouseEvent.BUTTON1) {
//			
//			EditDestPoint dest = getDestinationPoint(mouseMerc);
//			
//			if(mouseEditAction != null) {
//				//let the mouse edit action handle the press
//				mouseEditAction.mousePressed(dest);
//			}
//			else {
//				//do a selection with the press
//				
//				//store the latest point used for selection, for the move anchor
//				selectionPoint = dest;
//
//				Object selectObject = null;
//
//				//do a selection
//				if((activeSnapObject > -1)&&(activeSnapObject < snapObjects.size())) {	
//
//					//a little preprocessing
//					SnapObject snapObject = snapObjects.get(activeSnapObject);
//					selectionPoint.point = snapObject.snapPoint;
//					if(snapObject instanceof SnapNode) {
//						//use the node point as the start point
//						selectionPoint.snapNode = ((SnapNode)snapObject).node;
//					}
//
//					//get the edit object for this snap object
//					selectObject = snapObject.getSelectObject();
//				}
//
//				boolean wasVirtualNode = virtualNodeSelected;
//				boolean isVirtualNode = selectObject instanceof VirtualNode;
//
//				//handle selection
//
//				//check normal select or select node in a way
//				if((activeWay != null)&&(selectObject instanceof OsmNode)&&
//						(activeWay.getNodes().contains((OsmNode)selectObject))) {
//
//					//select a node within a way
//					int selectedIndex = activeWay.getNodes().indexOf((OsmNode)selectObject);
//					if(e.isShiftDown()) {
//						if(!selectedWayNodes.contains(selectedIndex)) {
//							selectedWayNodes.add(selectedIndex);
//						}
//						else {
//							selectedWayNodes.remove(selectedIndex);
//						}
//					}
//					else {
//						selectedWayNodes.clear();
//						selectedWayNodes.add(selectedIndex);
//					}
//				}
//				else {
//					//normal select action
//
//					//if shift is down do add/remove rather than replace selection
//					//except do not allow virtual nodes to be selected with anything else
//					boolean doAddRemove = ((e.isShiftDown())&&
//							(!isVirtualNode)&&(!wasVirtualNode));
//
//					if(doAddRemove) {
//						if(selectObject != null) {
//							if(selection.contains(selectObject)) {
//								selection.remove(selectObject);
//							}
//							else {
//								selection.add(selectObject);
//							}
//						}
//					}
//					else {
//						selection.clear();
//						if((selectObject != null)&&(!selection.contains(selectObject))) {
//							selection.add(selectObject);
//						}
//					}
//
//					//make sure selected nodes cleared
//					this.selectedWayNodes.clear();
//
//					//update the active way
//					if((selectObject instanceof OsmWay)&&(selection.size() == 1)) {
//						activeWay = (OsmWay)selectObject;
//					}
//					else {
//						activeWay = null;
//					}
//				}
//
//				virtualNodeSelected = isVirtualNode;
//
//				//report selection
//				termiteGui.setSelection(selection, selectedWayNodes);
//				this.getMapPanel().repaint();
//				
//			}
//		}
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
		if(e.getKeyCode() == KeyEvent.VK_M) {
			if(selectionActive) {
				inMove = true;
				moveImageToMerc.setTransform(imageToMerc);
				
				changed = true;
				loadMousePoint(moveMerc,movePix);
				updateMoveTransform();
				getMapPanel().repaint();
			}
		}
		
		if(changed) {
			getMapPanel().repaint();
		}
    }

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_M) {
			if(inMove) {
				inMove = false;
				getMapPanel().repaint();
			}
		}
    }
	
	@Override
	public void focusGained(FocusEvent e) {
		//it would be nice if we could get the key state for any keys that are help down
		//when focus is gained
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		if(inMove) {
			inMove = false;
			getMapPanel().repaint();
		}
	}
	
	// </editor-fold>
	
	//=====================
	// Private Methods
	//=====================
	
	private void renderPoint(Graphics2D g2, Point2D point) {
		int x = (int)point.getX();
		int y = (int)point.getY();
		g2.drawOval(x-RADIUS_PIX, y-RADIUS_PIX, 2*RADIUS_PIX, 2*RADIUS_PIX);
		g2.drawLine(x-RADIUS_PIX,y,x+RADIUS_PIX,y);
		g2.drawLine(x,y-RADIUS_PIX,x,y+RADIUS_PIX);
	}
	
	/** This method gets the location of the mouse in pixels. */
	private void loadMousePoint(Point2D mouseMerc, Point2D mousePix) {
		//get the current mouse location and update the nodes that move with the mouse
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();

		java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
		java.awt.Point mapPanelInApp = mapPanel.getLocationOnScreen();
		movePix.setLocation(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
		pixelsToMercator.transform(mousePix,mouseMerc);
	}
	
	private void updateMoveTransform() {
		if(selectionActive) {
			if(selectionP1) {
				//update translation
				double deltaX = moveMerc.getX() - p1Merc.getX();
				double deltaY = moveMerc.getY() - p1Merc.getY();
				workingTransform.setToTranslation(deltaX, deltaY);
				
				moveImageToMerc.setTransform(imageToMerc);
				moveImageToMerc.preConcatenate(workingTransform);
			}
			else {
				//update scale and rotation
				double length1 = p1Merc.distance(p2Merc);
				double length2 = p1Merc.distance(moveMerc);
				
//handle this case better
if((length1 == 0)||(length2 == 0)) return;
				
				double dmx1 = p2Merc.getX() - p1Merc.getX();
				double dmy1 = p2Merc.getY() - p1Merc.getY();
				double dmx2 = moveMerc.getX() - p1Merc.getX();
				double dmy2 = moveMerc.getY() - p1Merc.getY();
				double angle1 = Math.atan2(dmy1, dmx1);
				double angle2 = Math.atan2(dmy2, dmx2);
				
				double scale = length2 / length1;
				double rot = angle2 - angle1;
				
				moveImageToMerc.setTransform(imageToMerc);
				moveImageToMerc.translate(-p1Merc.getX(),-p1Merc.getY());
moveImageToMerc.translate(p1Image.getX(),p1Image.getY());
				moveImageToMerc.rotate(rot);
				moveImageToMerc.scale(scale,scale);
moveImageToMerc.translate(-p1Image.getX(),-p1Image.getY());
				moveImageToMerc.translate(p1Merc.getX(),p1Merc.getY());
				
			}
		}
	}
	
	private void completeMove() {
		//copy transform
		imageToMerc.setTransform(moveImageToMerc);
		//copy anchor points
		if(selectionActive) {
			if(selectionP1) {
				double deltaMX = moveMerc.getX() - p1Merc.getX();
				double deltaMY = moveMerc.getY() - p1Merc.getY();
				double deltaPX = movePix.getX() - p1Pix.getX();
				double deltaPY = movePix.getY() - p1Pix.getY();
				p1Merc.setLocation(moveMerc);
				p1Pix.setLocation(movePix);
				p2Merc.setLocation(p2Merc.getX() + deltaMX,p2Merc.getY() + deltaMY);
				p2Pix.setLocation(p2Pix.getX() + deltaPX,p2Pix.getY() + deltaPY);
			}
			else {
				
				
				
				p2Merc.setLocation(moveMerc);
				p2Pix.setLocation(movePix);
			}
		}
	}
	
}
