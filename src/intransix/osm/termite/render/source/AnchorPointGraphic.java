
package intransix.osm.termite.render.source;


import intransix.osm.termite.app.geocode.AnchorPoint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * This is an Anchor point for controlling geocoding of an image.
 * @author sutter
 */
public class AnchorPointGraphic extends Circle {
	
	private final static Color SELECT_COLOR = Color.RED;
	
	private AnchorPoint anchorPoint;	
	
	private Point2D layerPoint = new Point2D.Double();
	
//	/** This method renders the anchor point. */
//	public void renderPoint(Graphics2D g2,AffineTransform mercToPixels, 
//			boolean isSelected, boolean inMove, AffineTransform moveImageToMerc) {
//		
//		if(pointType == null) return;
//		
//		Color color = isSelected ? SELECT_COLOR : pointType.getColor();
//		g2.setColor(color);
//		
//		if(inMove) {
//			//if in move, transform from image to merc using the move transform
//			//then transform to pixels
//			moveImageToMerc.transform(imagePoint,pixPoint);
//			mercToPixels.transform(pixPoint,pixPoint);
//		}
//		else {
//			//not in move, transform straight from merc to pixels
//			mercToPixels.transform(mercPoint,pixPoint);
//		}
//		
//		int x = (int)pixPoint.getX();
//		int y = (int)pixPoint.getY();
//		g2.drawOval(x-RADIUS_PIX, y-RADIUS_PIX, 2*RADIUS_PIX, 2*RADIUS_PIX);
//		g2.drawLine(x-RADIUS_PIX,y,x+RADIUS_PIX,y);
//		g2.drawLine(x,y-RADIUS_PIX,x,y+RADIUS_PIX);
//	}
	
	public void updateLocation(AffineTransform mercToLayerTransform) {
		mercToLayerTransform.transform(anchorPoint.mercPoint,layerPoint);
		this.setCenterX(layerPoint.getX());
		this.setCenterY(layerPoint.getY());
	}
	
	public void updateScale(double pixelsToLayerScale) {
		this.setRadius(AnchorPoint.RADIUS_PIX * pixelsToLayerScale);
	}

}
