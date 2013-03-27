package intransix.osm.termite.render.tile;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.geometry.Bounds;
import javafx.scene.transform.Scale;

/**
 * This is a layer for a tile map source.
 * 
 * ENHANCEMENTS NEEDED:
 * - This class currently loads all tiles that are requested, whether or not
 * they are still needed by the time the http request starts. This should be fixed.
 * - On zooming this clears the old tiles before new tiles are added. The old tiles
 * should be cleared after the new tiles are added.
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
	
	public final static double MERC_MULTIPLIER_SCALE = (1 << 16);
	
	private int tileZoom = INVALID_ZOOM;
	
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
		
		//image doesn't work well if we use coordinates from 0-1 for world
		//some rounding takes place somewhere
		this.setPrefSize(MERC_MULTIPLIER_SCALE,MERC_MULTIPLIER_SCALE);
		this.setMinSize(MERC_MULTIPLIER_SCALE,MERC_MULTIPLIER_SCALE);
		this.setMaxSize(MERC_MULTIPLIER_SCALE,MERC_MULTIPLIER_SCALE);
		Scale scaleCorrectionForTiles = new Scale(1 / MERC_MULTIPLIER_SCALE,1 / MERC_MULTIPLIER_SCALE);
		this.getTransforms().setAll(scaleCorrectionForTiles); 
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
	
//	private void updateLayerCoordinates(int tileZoom, int pixelsPerTile) {
//		
//		double imagePixelsPerMerc = pixelsPerTile * 2^tileZoom;
//		
//		//image doesn't work well if we use coordinates from 0-1 for world
//		//some rounding takes place somewhere
//		this.setPrefSize(imagePixelsPerMerc,imagePixelsPerMerc);
//		this.setMinSize(imagePixelsPerMerc,imagePixelsPerMerc);
//		this.setMaxSize(imagePixelsPerMerc,imagePixelsPerMerc);
//		Scale scaleCorrectionForTiles = new Scale(1 / imagePixelsPerMerc,1 / imagePixelsPerMerc);
//		this.getTransforms().setAll(scaleCorrectionForTiles); 
//	}
	
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
				}
			}
		}
		
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
