package intransix.osm.termite.app.geocode.action;

import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.app.geocode.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.scene.input.MouseEvent;

/**
 * This MouseAction is used for a move in geocoding. 
 * 
 * @author sutter
 */
public class MoveAction implements GeocodeMouseAction {
	
	//=====================
	// Properties
	//=====================
	
	private GeocodeManager geocodeManager;
	private AffineTransform workingTransform = new AffineTransform();
	
	//=====================
	// Public Methods
	//=====================
	
	public MoveAction(GeocodeManager geocodeManager) {
		this.geocodeManager = geocodeManager;
	}
	
	public void initMove() {
		AnchorPoint anchorPoint = geocodeManager.getSelectedAnchorPoint();
		if((anchorPoint != null)&&(anchorPoint.mercPoint != null)) {
			geocodeManager.initMove();
			
//			Point2D mouseMerc = geocodeLayer.getMapPanel().getMousePointMerc();
//			updateMoveTransform(anchorPoint,mouseMerc);

//			geocodeLayer.notifyContentChange();
		}
	}
	
	/** This should return false if these if no move action. */
	@Override
	public boolean doMove() {
		return true;
	}
	
	@Override
	public void mouseMoved(Point2D mouseMerc, double mercPerPixelsScale, MouseEvent e) {
		AnchorPoint anchorPoint = geocodeManager.getSelectedAnchorPoint();
		if((anchorPoint != null)&&(anchorPoint.mercPoint != null)) {
			updateMoveTransform(anchorPoint,mouseMerc);
			
			geocodeManager.anchorPointsUpdated();
//			geocodeLayer.notifyContentChange();
		}
	}
	
	@Override
	public void mousePressed(Point2D mouseMerc, double mercPerPixelsScale, MouseEvent e) {
		AnchorPoint anchorPoint = geocodeManager.getSelectedAnchorPoint();
		if((anchorPoint != null)&&(anchorPoint.mercPoint != null)) {
			updateMoveTransform(anchorPoint,mouseMerc);
			geocodeManager.executeMove();
			
			//exit the move after a click
			geocodeManager.getGeocodeEditorMode().setLayerState(GeocodeEditorMode.LayerState.SELECT);
			
			geocodeManager.anchorPointsUpdated();
//			geocodeLayer.notifyContentChange();
		}
	}
	
	//=====================
	// Private Methods
	//=====================
	
	private void updateMoveTransform(AnchorPoint moveAnchor, Point2D moveMerc) {
		
		AnchorPoint p0 = geocodeManager.getAnchorPoints()[0];
		
		if((p0 == null)||(p0.mercPoint == null)) {
			return;
		}

		switch(moveAnchor.getPointType()) {
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
	
	private void translateTransform(Point2D moveMerc, Point2D anchorMerc) {
		AffineTransform imageToMerc = geocodeManager.getImageToMerc();
		AffineTransform moveImageToMerc = geocodeManager.getMoveImageToMerc();
		if((imageToMerc == null)||(moveImageToMerc == null)) return;
		
		//update translation
		double deltaX = moveMerc.getX() - anchorMerc.getX();
		double deltaY = moveMerc.getY() - anchorMerc.getY();
		workingTransform.setToTranslation(deltaX, deltaY);
		
		moveImageToMerc.setTransform(imageToMerc);
		moveImageToMerc.preConcatenate(workingTransform);
		
		geocodeManager.updateMove();
	}
	
	private void rotateScaleXYTransform(Point2D moveMerc, Point2D baseAnchorMerc,
			Point2D baseAnchorImage, Point2D moveAnchorMerc, 
			boolean doXScale, boolean doYScale) {
		
		AffineTransform imageToMerc = geocodeManager.getImageToMerc();
		AffineTransform moveImageToMerc = geocodeManager.getMoveImageToMerc();
		if((imageToMerc == null)||(moveImageToMerc == null)) return;

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
		
		workingTransform.setToRotation(rot,baseAnchorMerc.getX(),baseAnchorMerc.getY());
		
		moveImageToMerc.setTransform(imageToMerc);
		moveImageToMerc.translate(baseAnchorImage.getX(),baseAnchorImage.getY());
		moveImageToMerc.scale(scaleX,scaleY);
		moveImageToMerc.translate(-baseAnchorImage.getX(),-baseAnchorImage.getY());
		
		moveImageToMerc.preConcatenate(workingTransform);
		
		geocodeManager.updateMove();
	}
}