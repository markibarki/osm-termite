package intransix.osm.termite.render.source;

import intransix.osm.termite.render.*;
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
	
	private AffineTransform workingTransform = new AffineTransform();
	
	public void setMove(boolean inMove, AffineTransform moveImageToMerc) {
		this.inMove = inMove;
		this.moveImageToMerc = moveImageToMerc;
	}
	
	public AffineTransform getImageToMerc() {
		return imageToMerc;
	}
	
	public void setImageToMerc(AffineTransform imageToMerc) {
		this.imageToMerc = imageToMerc;
	}
	
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
		Composite activeComposite = this.getComposite();
		if(activeComposite != null) {
			g2.setComposite(activeComposite);
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
		if((infoflags & ImageObserver.ALLBITS) != 0) {
			//just do a repaint
			getMapPanel().repaint();
			return false;
		}
		else {
			return true;
		}
	}

	//=====================
	// Private Methods
	//=====================


}
