package intransix.osm.termite.render.source;

import intransix.osm.termite.app.maplayer.MapLayer;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.ImageObserver;
import java.io.File;

/**
 *
 * @author sutter
 */
public class SourceLayer extends MapLayer implements ImageObserver {
	
	private File imageFile;
	private Image sourceImage;

	private AffineTransform imageToMerc;
	private AffineTransform moveImageToMerc = new AffineTransform();
	private boolean inMove = false;
	
	public SourceLayer() {
		this.setName("Source Layer");
		this.setOrder(MapLayer.ORDER_OVERLAY_3);
		this.setVisible(false);
		this.setPreferredAngleRadians(0);
	}
	
	public void setMove(boolean inMove, AffineTransform moveImageToMerc) {
		this.inMove = inMove;
		this.moveImageToMerc = moveImageToMerc;
		this.notifyContentChange();
	}
	
	public AffineTransform getImageToMerc() {
		return imageToMerc;
	}
	
	public void setImageToMerc(AffineTransform imageToMerc) {
		this.imageToMerc = imageToMerc;
		
		double preferredAngleRad;
		if(imageToMerc != null) {
		
			double[] matrix = new double[6];
			imageToMerc.getMatrix(matrix);

			//this will only give good results for a rotation, not a skew
			preferredAngleRad = Math.atan2(matrix[1],matrix[0]);

		}
		else {
			preferredAngleRad = INVALID_ANGLE;
		}
		this.setPreferredAngleRadians(preferredAngleRad);
		
		this.notifyContentChange();
	}
	
	public boolean loadImage(File file) {
		//reset the transform - we need to add a way to sace this
		imageToMerc = null;
		
		try {
			sourceImage = java.awt.Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
			this.setVisible(true);
			return true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			this.setVisible(false);
			return false;
		}
	}
	
	public void clearImage() {
		sourceImage = null;
		imageToMerc = null;
		this.setVisible(false);
	}

	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform base = g2.getTransform();
		AffineTransform mercToPixels = getViewRegionManager().getMercatorToPixels();
		
		if(imageToMerc == null) {
			AffineTransform pixelsToMerc = getViewRegionManager().getPixelsToMercator();
			imageToMerc = new AffineTransform(pixelsToMerc);
		}

		//transform to tile coordinates
		g2.transform(mercToPixels);
		
		AffineTransform activeTransform = inMove ? moveImageToMerc : imageToMerc;
		g2.transform(activeTransform);
		
		if(sourceImage != null) {
			g2.drawImage(sourceImage, 0, 0, this);
		}
	}
	
	@Override
	public boolean imageUpdate(Image image, int infoflags, int x, int y, int width, int height) {
		boolean returnValue;
		boolean contentChanged;
		
		if((infoflags & ImageObserver.ALLBITS) != 0) {
			//just do a repaint
			contentChanged = true;
			returnValue = false;
		}
		else if((infoflags & ImageObserver.ABORT) != 0) {
			//just do a repaint, to continue with unloaded tiles
			contentChanged = true;
			returnValue = true;
		}
		else {
			contentChanged = false;
			returnValue = true;
		}
		
		if(contentChanged) {
			this.notifyContentChange();
		}
		
		return returnValue;
	}

	//=====================
	// Private Methods
	//=====================


}
