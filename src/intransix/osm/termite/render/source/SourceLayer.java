package intransix.osm.termite.render.source;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.util.JsonIO;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.transform.Transform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class SourceLayer extends MapLayer implements MapListener {
	
	private File imageFile;
	private Image sourceImage;
	private ImageView imageView;

	private Transform imageToMercFX = new Scale(1,1);
	private Transform moveImageToMercFX = new Scale(1,1);
	private boolean inMove = false;
	private Transform mercToLayerFX;
	private Transform layerToMercFX;
	
	private Transform savedImageToMercFX = null;
	
	public SourceLayer() {
		this.setName("Source Layer");
		this.setOrder(MapLayer.ORDER_OVERLAY_3);
		this.setPreferredAngleRadians(0);
	}
	
	/** This sets move coordinates for the image. If in move is true, the 
	 * image will be set at the move coordinates rather then the active coordinates. */
	public void setMove(boolean inMove, Transform moveImageToMercFX) {
		this.inMove = inMove;
		this.moveImageToMercFX = moveImageToMercFX;
		
		//update the image location
		updateLocation();
	}
	
	/** This method gets the image transform. */
	public Transform getImageToMercFX() {
		return imageToMercFX;
	}
	
	/** This method sets the active transform for the image. */
	public void setImageToMerc(Transform imageToMercFX) {
		this.imageToMercFX = imageToMercFX;
		
		double preferredAngleRad;
		if(imageToMercFX != null) {

			//this will only give good results for a rotation, not a skew
			preferredAngleRad = Math.atan2(imageToMercFX.getMyx(),imageToMercFX.getMxx());

		}
		else {
			preferredAngleRad = INVALID_ANGLE;
		}
		this.setPreferredAngleRadians(preferredAngleRad);
		
		//update the image location
		updateLocation();
	}
	
	/** This method loads the image from a file and loads the associated transform
	 * if one exists. */
	public boolean loadImage(File file) {
		
		try {
			imageFile = file;
			InputStream is = new FileInputStream(file);
			this.sourceImage = new Image(is);

			imageView = new ImageView(sourceImage);
			
			//load the saved transform
			boolean transformLoaded = loadTransform();
			if(!transformLoaded) {
				//if transform not loaded, create one
//for now stick it in the latest pixel coordinates - UPDATE THIS TO FIT THE CURRENT SCREEN
				this.imageToMercFX = this.layerToMercFX;
			}
			
//testing
if(sourceImage != null) {
	double height = sourceImage.getHeight();
	double width = sourceImage.getWidth();
	if(height <= 0) {
		height = 1000;
	}
	if(width <= 0) {
		width = 1000;
	}
	
	imageView.setX(0);
	imageView.setY(0);
	imageView.setFitHeight(height);
	imageView.setFitWidth(width);
}
			
			this.updateLocation();
			
			return true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			this.setVisible(false);
			return false;
		}
	}
	
	public void clearImage() {
		imageView = null;
		sourceImage = null;
		imageFile = null;
		
		imageToMercFX = new Scale(1,1);
		moveImageToMercFX = new Scale(1,1);
		inMove = false;

	}

	public void storeChanges() {
		maybeSaveTransform();
	}

	
//	@Override
//	public void render(Graphics2D g2) {
//		
//		AffineTransform base = g2.getTransform();
//		AffineTransform mercToPixels = getViewRegionManager().getMercatorToPixels();
//		
//		if(imageToMerc == null) {
//			AffineTransform pixelsToMerc = getViewRegionManager().getPixelsToMercator();
//			imageToMerc = new AffineTransform(pixelsToMerc);
//		}
//
//		//transform to tile coordinates
//		g2.transform(mercToPixels);
//		
//		AffineTransform activeTransform = inMove ? moveImageToMerc : imageToMerc;
//		g2.transform(activeTransform);
//		
//		if(sourceImage != null) {
//			g2.drawImage(sourceImage, 0, 0, this);
//		}
//	}
//	
//	@Override
//	public boolean imageUpdate(Image image, int infoflags, int x, int y, int width, int height) {
//		boolean returnValue;
//		boolean contentChanged;
//		
//		if((infoflags & ImageObserver.ALLBITS) != 0) {
//			//just do a repaint
//			contentChanged = true;
//			returnValue = false;
//		}
//		else if((infoflags & ImageObserver.ABORT) != 0) {
//			//just do a repaint, to continue with unloaded tiles
//			contentChanged = true;
//			returnValue = true;
//		}
//		else {
//			contentChanged = false;
//			returnValue = true;
//		}
//		
//		if(contentChanged) {
//			this.notifyContentChange();
//		}
//		
//		return returnValue;
//	}
//
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {	
		if(zoomChanged) {
			mercToLayerFX = viewRegionManager.getMercatorToPixelsFX();
			layerToMercFX = viewRegionManager.getPixelsToMercatorFX();
		
			//update image location
			updateLocation();
		}
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {}
	
	@Override
	public void onLocalCoordinatesSet(ViewRegionManager vrm) {}	
	
	
	//=====================
	// Private Methods
	//=====================
		
	/** This metho sets the image at the proper coordinates. */
	private void updateLocation() {
		//update the image location
		if(imageView == null) return;
		
		if(inMove) {
			imageView.getTransforms().setAll(moveImageToMercFX,mercToLayerFX);
		}
		else {
			imageView.getTransforms().setAll(imageToMercFX,mercToLayerFX);
		}
	}
	
	private final static String TRANSFORM_FILE_SUFFIX = ".geo";

	private void maybeSaveTransform() {
		if((imageFile != null)&&(imageToMercFX != null)&&(!imageToMercFX.equals(savedImageToMercFX))) {
			try {
				String transformPath = imageFile.getAbsolutePath() + TRANSFORM_FILE_SUFFIX;

				List<Double> transformList = new ArrayList<>();
				transformList.add(imageToMercFX.getMxx());
				transformList.add(imageToMercFX.getMyx());
				transformList.add(imageToMercFX.getMxy());
				transformList.add(imageToMercFX.getMyy());
				transformList.add(imageToMercFX.getTx());
				transformList.add(imageToMercFX.getTy());
				
				JSONObject json = new JSONObject();
				json.put("transform",transformList);
				JsonIO.writeJsonFile(transformPath,json);

				//update the saved value
				savedImageToMercFX = imageToMercFX;
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	private boolean loadTransform() {
		if(imageFile != null) {
			try {
				String transformPath = imageFile.getAbsolutePath() + TRANSFORM_FILE_SUFFIX;
				File file = new File(transformPath);
				if(file.exists()) {
					JSONObject json = JsonIO.readJsonFile(transformPath);
					JSONArray transformJson = json.optJSONArray("transform");
					if(transformJson != null) {
						imageToMercFX = Transform.affine(transformJson.getDouble(0),
								transformJson.getDouble(1),
								transformJson.getDouble(2),
								transformJson.getDouble(3),
								transformJson.getDouble(4),
								transformJson.getDouble(5));

						//cache the saved value
						savedImageToMercFX = imageToMercFX;
					}
					
					return true;
				}
				else {
					return false;
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}


}
