package intransix.osm.termite.render.tile;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.geometry.Bounds;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/**
 * This is a layer for a tile map source. I jumped through a few hoops to get 
 * javafx to do this without rounding problems. I am sure there is a better way
 * to handle the transformation.
 * 
 * ENHANCEMENTS NEEDED:
 * - This class currently loads all tiles that are requested, whether or not
 * they are still needed by the time the http request starts. This should be fixed.
 * - On zooming this clears the old tiles before new tiles are added. The old tiles
 * should be cleared after the new tiles are added. However, some updates to the 
 * scale/transformation mechanism would be needed.
 * 
 * @author sutter
 */
public class TileLayer extends MapLayer implements MapListener {
	
	//=========================
	// Properties
	//=========================
	
	public final static int MAX_CACHE_SIZE = 150;
	public final static int INVALID_ZOOM = -1;
	
	public final static int MAX_ZOOM_EXCESS = 4;
	
	public final static double MERC_MULTIPLIER_SCALE = (1 << 18);
	
	private int tileZoom = INVALID_ZOOM;
	private AffineTransform mercToTileLayerTransform;
	private double mercToTileLayerScale;
	private Transform tileLayerToMercTransformFX;
	
	private HashMap<String,Tile> tileCache = new HashMap<>();
	private TileInfo tileInfo;
	
	private final ArrayList<Tile> workingTiles = new ArrayList<>();
	
	private ViewRegionManager viewRegionManager;

	//=========================
	// Public Methods
	//=========================
	
	/** Constructor. */
	public TileLayer() {
		this.setName("Base Map");
		this.setOrder(MapLayer.ORDER_BASE_MAP_1);
		this.setVisible(false);
		
		this.tileZoom = INVALID_ZOOM;

		mercToTileLayerScale = MERC_MULTIPLIER_SCALE;
		mercToTileLayerTransform = AffineTransform.getScaleInstance(mercToTileLayerScale, mercToTileLayerScale);
		tileLayerToMercTransformFX = Affine.scale(1/mercToTileLayerScale,1/mercToTileLayerScale);	
	}
	
	/** This method sets the view region manager. */
	public void setViewRegionManager(ViewRegionManager viewRegionManager) {
		this.viewRegionManager = viewRegionManager;
	}
	
	/** This sets the tile info for the active tile set. */
	public void setTileInfo(TileInfo tileInfo) {
		this.tileInfo = tileInfo;
		if(tileInfo != null) {
			this.setVisible(true);
			//update zoom if needed
			setZoomScale(viewRegionManager);
			//update the tiles
			updateTiles(viewRegionManager);
		}
		else {
			this.setVisible(false);
			//clear tiles
			this.getChildren().clear();
		}
		
		
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {	
		if((zoomChanged)||(tileZoom == INVALID_ZOOM)) {
			setZoomScale(viewRegionManager);
		}
		updateTiles(viewRegionManager);
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {}
	
	@Override
	public void onLocalCoordinatesSet(ViewRegionManager vrm) {}

	//=================================
	// Private Methods
	//=================================
	
	/** This method picks an optimal scale for this given viewport. */
	private void setZoomScale(ViewRegionManager viewRegionManager) {
		if(tileInfo == null) return;
		
		double pixelsPerMerc = viewRegionManager.getZoomScalePixelsPerMerc();
		double tilesPerMerc = pixelsPerMerc / tileInfo.getTileSize();
		int desiredScale = (int)Math.round(Math.log(tilesPerMerc)/Math.log(2));
	
		if(desiredScale > tileInfo.getMaxZoom()) {
			tileZoom = tileInfo.getMaxZoom();
		}
		else if (desiredScale < tileInfo.getMinZoom()) {
			tileZoom = tileInfo.getMinZoom();
		}
		else {
			tileZoom = desiredScale;
		}		
		
		//on a zoom change, update the coordinates of the layer
		//the update is applied when the tiles are changed
		
		//create a copy of this transform
		mercToTileLayerTransform = new AffineTransform(viewRegionManager.getMercatorToPixels());
		mercToTileLayerScale = viewRegionManager.getZoomScalePixelsPerMerc();
		//no copy needed here because these are static for now
		Affine atFX = viewRegionManager.getPixelsToMercatorFX();
		tileLayerToMercTransformFX = Affine.affine(atFX.getMxx(), atFX.getMyx(), atFX.getMxy(), atFX.getMyy(), atFX.getTx(), atFX.getTy());
	}
	
	/** This method updates the active tiles. */
	private void updateTiles(ViewRegionManager viewRegionManager) {
		if(tileInfo == null) return;
		
		//get the bounds in merc coordinates
		Bounds bounds = viewRegionManager.getPixelRect();
		AffineTransform at = viewRegionManager.getPixelsToMercator();
		Point2D temp = new Point2D.Double();
		double[] mercRange = {1.0,1.0,0.0,0.0};
		temp.setLocation(0,0);
		updateMercRange(temp,at,mercRange);
		temp.setLocation(0,bounds.getHeight());
		updateMercRange(temp,at,mercRange);
		temp.setLocation(bounds.getWidth(),0);
		updateMercRange(temp,at,mercRange);
		temp.setLocation(bounds.getWidth(),bounds.getHeight());
		updateMercRange(temp,at,mercRange);
		
		//get tile range
		int numTiles = (1 << tileZoom);
		int minTileX = (int)(numTiles * mercRange[0]);
		if(minTileX < 0) minTileX = 0;
		int minTileY = (int)(numTiles * mercRange[1]);
		if(minTileY < 0) minTileY = 0;
		int maxTileX = (int)(numTiles * mercRange[2]);
		if(maxTileX >= numTiles) maxTileX = numTiles - 1;
		int maxTileY = (int)(numTiles * mercRange[3]);
		if(maxTileY >= numTiles) maxTileY = numTiles - 1;
		
System.out.println("zoom: " + tileZoom + " range: " + minTileX + "," + minTileY + "-" +  maxTileX + "," + maxTileY);
		
		//load the tiles
		workingTiles.clear();
		Tile tile;
		for(int ix = minTileX; ix <= maxTileX; ix++) {
			for(int iy = minTileY; iy <= maxTileY; iy++) {
				tile = getTile(ix,iy,tileZoom);
				if(tile != null) {
					workingTiles.add(tile);
					tile.setLocation(mercToTileLayerTransform, mercToTileLayerScale);
				}
			}
		}
		
		this.getChildren().clear();
		//set the transform used for these tiles - it will be updated on zooming but not pan for now.
		this.getTransforms().setAll(tileLayerToMercTransformFX); 
		this.getChildren().setAll(workingTiles);
		workingTiles.clear();
	}
	
	/** This updates the mercRange for the given pixel point and pixel to merc transform. */
	private void updateMercRange(Point2D point, AffineTransform pixelsToMerc, double[] mercRange) {
		pixelsToMerc.transform(point, point);
		if(point.getX() < mercRange[0]) mercRange[0] = point.getX();
		if(point.getY() < mercRange[1]) mercRange[1] = point.getY();
		if(point.getX() > mercRange[2]) mercRange[2] = point.getX();
		if(point.getY() > mercRange[3]) mercRange[3] = point.getY();
	}
	
	/** This method retrieves a tile. */
	private Tile getTile(int ix, int iy, int zoom) {	
		String url = tileInfo.getUrl(ix, iy, zoom);
		Tile tile = tileCache.get(url);
		if(tile == null) {
			tile = new Tile(ix, iy, zoom, tileInfo.getTileSize(), url);
			
			//remove the oldest tile, if necessary
			if(tileCache.size() > MAX_CACHE_SIZE) {
				long oldestTime = Long.MAX_VALUE;
				Tile oldestTile = null;
				long currentTime;
				for(Tile currentTile:tileCache.values()) {
					currentTime = currentTile.getActiveTime();
					if(currentTime < oldestTime) {
						oldestTime = currentTime;
						oldestTile = currentTile;
					}
				}
				if(oldestTile != null) {
					tileCache.remove(oldestTile.getUrl());
				}
			}
				
			tileCache.put(url, tile);
		}
		
		//set the active time
		tile.setActive(System.currentTimeMillis());
		return tile;
	}

		
}
