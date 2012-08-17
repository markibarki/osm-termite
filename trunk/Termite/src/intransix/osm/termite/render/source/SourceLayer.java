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
	
	public SourceLayer(MapLayerManager mapLayerManager) {
		super(mapLayerManager);
		this.setName("Source Layer");
		this.setVisible(false);
	}
	
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
	
	public double getAngleRad() {
		if(imageToMerc == null) return 0;
		
		double[] matrix = new double[6];
		imageToMerc.getMatrix(matrix);
		
		//this will only give good results for a rotation, not a skew
		double ang1 = Math.atan2(matrix[1],matrix[0]);
		return ang1;
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
		
		MapPanel mapPanel = getMapPanel();
		AffineTransform mercToPixels = mapPanel.getMercatorToPixels();
		
		if(imageToMerc == null) {
			AffineTransform pixelsToMerc = mapPanel.getPixelsToMercator();
			Rectangle visibleRect = mapPanel.getVisibleRect();
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
