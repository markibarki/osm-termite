package intransix.osm.termite.render.source;


import intransix.osm.termite.render.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 *
 * @author sutter
 */
public class GeocodeLayer extends MapLayer implements
		MouseListener, MouseMotionListener, KeyListener, FocusListener {
	
	public enum LayerState {
		INACTIVE,
		SELECT,
		PLACE_P0,
		PLACE_P1,
		PLACE_P2,
		MOVE
	}
	
	public enum GeocodeType {
		TWO_POINT,
		THREE_POINT_ORTHO,
		FREE_TRANSFORM
	}
	
	private final static int INVALID_SELECTION = -1;
	
	private GeocodeType geocodeType = GeocodeType.TWO_POINT;
	private LayerState layerState = LayerState.SELECT;
	private int selection = INVALID_SELECTION;
	private boolean inMove = false;
	
	private SourceLayer sourceLayer;
	
	private AnchorPoint[] anchorPoints;
	private AnchorPoint p0 = new AnchorPoint();
	private AnchorPoint p1 = new AnchorPoint();
	private AnchorPoint p2 = new AnchorPoint();
	
	private AffineTransform imageToMerc;
	private AffineTransform mercToImage;
	private AffineTransform moveImageToMerc = new AffineTransform();
	
	private AffineTransform workingTransform = new AffineTransform();
	private Point2D moveMerc = new Point2D.Double();
	private Point2D movePix = new Point2D.Double();
	
	public GeocodeLayer() {
		anchorPoints = new AnchorPoint[3];
		anchorPoints[0] = p0;
		anchorPoints[1] = p1;
		anchorPoints[2] = p2;
	}
	
	public void setSourceLayer(SourceLayer sourceLayer) {
		this.sourceLayer = sourceLayer;
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
				
				//get active data
				imageToMerc = sourceLayer.getImageToMerc();
				if(imageToMerc != null) {
					updateInverseTransform();
				}
			}
			else {
				mapPanel.removeMouseListener(this);
				mapPanel.removeMouseMotionListener(this);
				mapPanel.removeKeyListener(this);
				mapPanel.removeFocusListener(this);
				
				cleanUp();
			}
		}
	}
	
	public void setGeocodeType(GeocodeType geocodeType) {
		this.geocodeType = geocodeType;
	}
	
	public void setLayerState(LayerState layerState) {
		if(imageToMerc != null) {
			this.layerState = layerState;
		}
		else {
			this.layerState = LayerState.INACTIVE;
		}
	}
	
	@Override
	public void render(Graphics2D g2) {
				
		//set the opacity for the layer
		Composite originalComposite = null;
		Composite activeComposite = this.getComposite();
		if(activeComposite != null) {
			originalComposite = g2.getComposite();
			g2.setComposite(activeComposite);
		}
		
		MapPanel mapPanel = getMapPanel();
		AffineTransform mercToPixels = mapPanel.getMercatorToPixels();
		
		//draw the points
		AnchorPoint ap;
		boolean isSelected;
		for(int i = 0; i < 3; i++) {
			ap = anchorPoints[i];
			if(ap.mercPoint != null) {
				isSelected = (selection == i);
				ap.renderPoint(g2,mercToPixels,isSelected,inMove,moveImageToMerc);
			}
		}
		
		if(originalComposite != null) {
			g2.setComposite(originalComposite);
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
			updateMoveTransform(moveMerc);
			
			sourceLayer.setMove(true, moveImageToMerc);
			mapPanel.repaint();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		if(layerState == LayerState.INACTIVE) return;
	
		boolean changed = false;
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
		double mercPerPixelsScale = 1 / mapPanel.getZoomScalePixelsPerMerc();
		
		Point2D mouseMerc = new Point2D.Double();
		mouseMerc.setLocation(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc,mouseMerc);
		
		if(layerState == LayerState.PLACE_P0) {
			
			p0.mercPoint = mouseMerc;
			p0.imagePoint = new Point2D.Double();
			mercToImage.transform(mouseMerc,p0.imagePoint);
			
			if(geocodeType == GeocodeType.FREE_TRANSFORM) {
				p0.pointType = AnchorPoint.PointType.FREE_TRANSFORM;
			}
			else {
				p0.pointType = AnchorPoint.PointType.TRANSLATE;
			}
			
			changed = true;
		}
		else if(layerState == LayerState.PLACE_P1) {
			
			p1.mercPoint = mouseMerc;
			p1.imagePoint = new Point2D.Double();
			mercToImage.transform(mouseMerc,p1.imagePoint);
			
			if(geocodeType == GeocodeType.FREE_TRANSFORM) {
				p1.pointType = AnchorPoint.PointType.FREE_TRANSFORM;
			}
			else {
				setScalePointTypes();
			}
			
			changed = true;
		}
		else if(layerState == LayerState.PLACE_P2) {
			
			p2.mercPoint = mouseMerc;
			p2.imagePoint = new Point2D.Double();
			mercToImage.transform(mouseMerc,p2.imagePoint);
			
			if(geocodeType == GeocodeType.FREE_TRANSFORM) {
				p2.pointType = AnchorPoint.PointType.FREE_TRANSFORM;
			}
			else {
				setScalePointTypes();
			}
			
			changed = true;
		}
		else if(layerState == LayerState.SELECT) {
			if((inMove)&&(selection != INVALID_SELECTION)) {
				
				updateMoveTransform(mouseMerc);
				completeMove();
				changed = true;
				
				//clear selection
				selection = INVALID_SELECTION;
			}
			else {
				selection = INVALID_SELECTION;
				
				AnchorPoint ap;
				for(int i = 0; i < 3; i++) {
					ap = anchorPoints[i];
					if(ap.hitCheck(mouseMerc,mercPerPixelsScale)) {
						selection = i;
					}
				}
				
				changed = true;
			}
		}
		
		if(changed) {
			mapPanel.repaint();
		}
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
			if(selection != INVALID_SELECTION) {
				inMove = true;
				moveImageToMerc.setTransform(imageToMerc);
				
				Point2D mouseMerc = getMouseMerc();
				updateMoveTransform(mouseMerc);
				changed = true;
				
				sourceLayer.setMove(true, moveImageToMerc);
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
	
	private void setScalePointTypes() {
		Point2D ip1 = p1.imagePoint;
		Point2D ip2 = p2.imagePoint;
		
		if((ip1 != null)&&(ip2 != null)) {
			double tan1 = Math.abs(ip1.getY())/(Math.abs(ip1.getX()) + .001);
			double tan2 = Math.abs(ip2.getY())/(Math.abs(ip2.getX()) + .001);
			if(tan1 < tan2) {
				p1.pointType = AnchorPoint.PointType.ROTATE_SCALE_X;
				p2.pointType = AnchorPoint.PointType.ROTATE_SCALE_Y;
			}
			else {
				p1.pointType = AnchorPoint.PointType.ROTATE_SCALE_Y;
				p2.pointType = AnchorPoint.PointType.ROTATE_SCALE_X;
			}
		}
		else {
			p1.pointType = AnchorPoint.PointType.ROTATE_SCALE_XY;
			p2.pointType = AnchorPoint.PointType.ROTATE_SCALE_XY;
		}
	}
	
	/** This method gets the location of the mouse in pixels. */
	private Point2D getMouseMerc() {
		//get the current mouse location and update the nodes that move with the mouse
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();

		Point2D mouseMerc = new Point2D.Double();
		java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
		java.awt.Point mapPanelInApp = mapPanel.getLocationOnScreen();
		mouseMerc.setLocation(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
		pixelsToMercator.transform(mouseMerc,mouseMerc);
		return mouseMerc;
	}
	
	private void updateMoveTransform(Point2D moveMerc) {
		if(selection != INVALID_SELECTION) {
			AnchorPoint moveAnchor = anchorPoints[selection];
			if(moveAnchor.mercPoint != null) {
				
				switch(moveAnchor.pointType) {
					case TRANSLATE:
						translateTransform(moveMerc,moveAnchor.mercPoint);
						break;

					case ROTATE_SCALE_XY:
						if(p0.mercPoint != null) {
							rotateScaleXYTransform(moveMerc,p0.mercPoint,
									p0.imagePoint,moveAnchor.mercPoint,
									true,true);
						}
						break;

					case ROTATE_SCALE_X:
						if(p0.mercPoint != null) {
							rotateScaleXYTransform(moveMerc,p0.mercPoint,
									p0.imagePoint,moveAnchor.mercPoint,
									true,false);
						}
						break;

					case ROTATE_SCALE_Y:
						if(p0.mercPoint != null) {
							rotateScaleXYTransform(moveMerc,p0.mercPoint,
									p0.imagePoint,moveAnchor.mercPoint,
									false,true);
						}
						break;
				}
			}
		}
	}
	
	private void translateTransform(Point2D moveMerc, Point2D anchorMerc) {
		//update translation
		double deltaX = moveMerc.getX() - anchorMerc.getX();
		double deltaY = moveMerc.getY() - anchorMerc.getY();
		workingTransform.setToTranslation(deltaX, deltaY);

		moveImageToMerc.setTransform(imageToMerc);
		moveImageToMerc.preConcatenate(workingTransform);
	}
	
	private void rotateScaleXYTransform(Point2D moveMerc, Point2D baseAnchorMerc,
			Point2D baseAnchorImage, Point2D moveAnchorMerc, 
			boolean doXScale, boolean doYScale) {

		//update scale and rotation
		double length1 = baseAnchorMerc.distance(moveAnchorMerc);
		double length2 = baseAnchorMerc.distance(moveMerc);

//handle this case better
if((length1 == 0)||(length2 == 0)) return;

		double dmx1 = moveAnchorMerc.getX() - baseAnchorMerc.getX();
		double dmy1 = moveAnchorMerc.getY() - baseAnchorMerc.getY();
		double dmx2 = moveMerc.getX() - baseAnchorMerc.getX();
		double dmy2 = moveMerc.getY() - baseAnchorMerc.getY();
		double angle1 = Math.atan2(dmy1, dmx1);
		double angle2 = Math.atan2(dmy2, dmx2);

		double scale = length2 / length1;
		double rot = angle2 - angle1;

		double scaleX = doXScale ? scale : 1;
		double scaleY = doYScale ? scale : 1;

		moveImageToMerc.setTransform(imageToMerc);
		moveImageToMerc.translate(-baseAnchorMerc.getX(),-baseAnchorMerc.getY());
		moveImageToMerc.translate(baseAnchorImage.getX(),baseAnchorImage.getY());
		moveImageToMerc.rotate(rot);
		moveImageToMerc.scale(scaleX,scaleY);
		moveImageToMerc.translate(-baseAnchorImage.getX(),-baseAnchorImage.getY());
		moveImageToMerc.translate(baseAnchorMerc.getX(),baseAnchorMerc.getY());
	}
	
	private void completeMove() {
		//copy transform
		imageToMerc.setTransform(moveImageToMerc);
		updateInverseTransform();
		for(AnchorPoint ap:anchorPoints) {
			if(ap.mercPoint != null) {
				imageToMerc.transform(ap.imagePoint, ap.mercPoint);
			}
		}
		sourceLayer.setMove(false,null);
		sourceLayer.setImageToMerc(imageToMerc);
	}
	
	private void updateInverseTransform() {
		try {
			mercToImage = imageToMerc.createInverse();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void cleanUp() {

		layerState = LayerState.INACTIVE;
		selection = INVALID_SELECTION;
		inMove = false;
		
		for(AnchorPoint ap:anchorPoints) {
			ap.reset();
		}
	
	
		imageToMerc = null;
		mercToImage = null;
	}
	
}