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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

	private AffineTransform imageToMerc = new AffineTransform();
	private AffineTransform moveImageToMerc = new AffineTransform();
	private Transform imageToMercFX = new Scale(1,1);
	private Transform moveImageToMercFX = new Scale(1,1);
	private boolean inMove = false;
	private Transform mercToLayerFX;
	private Transform layerToMercFX;
	private AffineTransform layerToMerc;
	
	private Transform savedImageToMercFX = null;
	
	private SimpleBooleanProperty isActive = new SimpleBooleanProperty(false);
	
	public SourceLayer() {
		this.setName("Source Layer");
		this.setOrder(MapLayer.ORDER_OVERLAY_3);
		this.setPreferredAngleRadians(0);
	}
	
	/** This is used to manage the active and inactive source layers. */
	public void setIsActive(Boolean isActive) {
		this.isActive.setValue(isActive);
	}
	
	/** This is used to manage the active and inactive source layers. */
	public Boolean getIsActive() {
		return isActive.getValue();
	}
	
	public BooleanProperty getIsActiveProperty() {
		return isActive;
	}
	
	/** This sets move coordinates for the image. If in move is true, the 
	 * image will be set at the move coordinates rather then the active coordinates. */
	public void setMove(boolean inMove, AffineTransform moveImageToMerc) {
		this.inMove = inMove;
		this.moveImageToMerc = moveImageToMerc;
		this.moveImageToMercFX = Transform.affine(moveImageToMerc.getScaleX(),
					moveImageToMerc.getShearY(),
					moveImageToMerc.getShearX(),
					moveImageToMerc.getScaleY(),
					moveImageToMerc.getTranslateX(),
					moveImageToMerc.getTranslateY());
		
		//update the image location
		updateLocation();
	}
	
	/** This method gets the image transform. */
	public AffineTransform getImageToMerc() {
		return imageToMerc;
	}
	
	/** This method sets the active transform for the image. */
	public void setImageToMerc(AffineTransform imageToMerc) {
		this.imageToMerc = imageToMerc;
		imageToMercFX = Transform.affine(imageToMerc.getScaleX(),
					imageToMerc.getShearY(),
					imageToMerc.getShearX(),
					imageToMerc.getScaleY(),
					imageToMerc.getTranslateX(),
					imageToMerc.getTranslateY());
		
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
//(panning will screw this up)
				this.imageToMercFX = this.layerToMercFX;
				AffineTransform at = new AffineTransform(layerToMerc);
				this.setImageToMerc(at);
			}
			
			this.updateLocation();

			//add image
			getChildren().add(imageView);
			
			return true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			this.setVisible(false);
			return false;
		}
	}
	
	public void clearImage() {
		this.getChildren().clear();
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

	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {	
		if(zoomChanged) {
			mercToLayerFX = viewRegionManager.getMercatorToPixelsFX();
			layerToMerc = viewRegionManager.getPixelsToMercator();
			layerToMercFX = viewRegionManager.getPixelsToMercatorFX();
			
			this.getTransforms().setAll(layerToMercFX);
		
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
			imageView.getTransforms().setAll(mercToLayerFX,moveImageToMercFX);
		}
		else {
			imageView.getTransforms().setAll(mercToLayerFX,imageToMercFX);
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
						imageToMerc = new AffineTransform(transformJson.getDouble(0),
								transformJson.getDouble(1),
								transformJson.getDouble(2),
								transformJson.getDouble(3),
								transformJson.getDouble(4),
								transformJson.getDouble(5));
						
						//this will do all the necessary updates, including the FX transfor
						this.setImageToMerc(imageToMerc);

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
