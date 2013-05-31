package intransix.osm.termite.render.source;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.ImageObserver;
import java.io.File;
import intransix.osm.termite.util.JsonIO;
import javax.swing.JOptionPane;
import org.json.*;

/**
 *
 * @author sutter
 */
public class SourceLayer extends MapLayer implements ImageObserver {

	private final static String TRANSFORM_TAG = "transform";
	
	private File imageFile;
	private Image sourceImage;
	private JSONObject configJson = null;
	private boolean loadError = false;

	private AffineTransform imageToMerc;
	private AffineTransform moveImageToMerc = new AffineTransform();
	private boolean inMove = false;

	private AffineTransform savedImageToMerc;
	
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
	
	public boolean loadImage(File file, ViewRegionManager vrm) {
		//reset the transform - we need to add a way to sace this
		imageToMerc = null;
		
		try {
			sourceImage = java.awt.Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
			//kick off loading the image
			sourceImage.getHeight(this);

			imageFile = file;
			loadTransform(vrm);
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

	public void storeChanges() {
		maybeSaveTransform();
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
			returnValue = false;
			if(!loadError) {
				loadError = true;
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,"Error: Unable to load image!");
					}
				});
				thread.start();
			}
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
	
	private final static String TRANSFORM_FILE_SUFFIX = ".geo";

	private void maybeSaveTransform() {
		if((imageFile != null)&&(imageToMerc != null)&&(!imageToMerc.equals(savedImageToMerc))) {
			try {
				String transformPath = imageFile.getAbsolutePath() + TRANSFORM_FILE_SUFFIX;
				double[] matrix = new double[6];
				imageToMerc.getMatrix(matrix);
				if(configJson == null) {
					configJson = new JSONObject();
				}
				configJson.put(TRANSFORM_TAG,matrix);
				JsonIO.writeJsonFile(transformPath,configJson);

				//update the saved value
				savedImageToMerc = new AffineTransform(imageToMerc);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	private void loadTransform(ViewRegionManager vrm) {
		if(imageFile != null) {
			try {
				String transformPath = imageFile.getAbsolutePath() + TRANSFORM_FILE_SUFFIX;
				File file = new File(transformPath);
				if(file.exists()) {
					configJson = JsonIO.readJsonFile(transformPath);
					JSONArray transformJson = configJson.optJSONArray(TRANSFORM_TAG);
					if(transformJson != null) {
						imageToMerc = new AffineTransform();
						imageToMerc.setTransform(transformJson.getDouble(0),transformJson.getDouble(1),transformJson.getDouble(2),
								transformJson.getDouble(3),transformJson.getDouble(4),transformJson.getDouble(5));

						//cache the saved value
						savedImageToMerc = new AffineTransform(imageToMerc);

						//zoom to this input
						Point2D p1 = new Point2D.Double(0,0);
						//tryu to read the image dimensions = may fail
						double width = sourceImage.getWidth(null);
						double height = sourceImage.getWidth(null);
						if(width <= 0) width = 500;
						if(height <= 0) height = 500;
						Point2D p2 = new Point2D.Double(width,height);

						//set to the image boundary with some padding
						imageToMerc.transform(p1, p1);
						imageToMerc.transform(p2, p2);
						double mercCenterX = (p2.getX() + p1.getX())/2;
						double mercCenterY = (p2.getY() + p1.getY())/2;
						double mercWidth = Math.abs(p2.getX() - p1.getX());
						double mercHeight = Math.abs(p2.getY() - p1.getY());
						Rectangle2D mercRect = new Rectangle2D.Double(mercCenterX - mercWidth,mercCenterY - mercHeight,2 * mercWidth,2 * mercHeight);
						vrm.setMercViewBounds(mercRect);
					}
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}


}
