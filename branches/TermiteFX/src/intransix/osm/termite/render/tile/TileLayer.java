package intransix.osm.termite.render.tile;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.maplayer.PaneLayer;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;

/**
 * This is a layer for a tile map source.
 * 
 * @author sutter
 */
public class TileLayer extends PaneLayer implements MapListener {
	
	//=========================
	// Properties
	//=========================
	
	public final static int MAX_CACHE_SIZE = 150;
	public final static int INVALID_ZOOM = -1;
	
	public final static int MAX_ZOOM_EXCESS = 4;
	
	public final static double MERC_MULTIPLIER_SCALE = (1 << 25);
	
	private int tileZoom = INVALID_ZOOM;
	
	private HashMap<String,Tile> tileCache = new HashMap<>();
	private int minZoom;
	private int maxZoom;
	private int pixelsPerTile;
	private TileInfo tileInfo;
	
	private final ArrayList<Tile> workingTiles = new ArrayList<>();
	private final Scale scaleCorrectionForTiles = new Scale(1 / MERC_MULTIPLIER_SCALE,1 / MERC_MULTIPLIER_SCALE);
	
	private ViewRegionManager viewRegionManager;

	//=========================
	// Public Methods
	//=========================
	
	public void connect(MapLayerManager mapLayerManager){}
	
	public void disconnect(MapLayerManager mapLayerManager){}

	
	public TileLayer() {
		this.setName("Base Map");
		this.setOrder(MapLayer.ORDER_BASE_MAP_1);
		this.setVisible(false);
		
		//hard code to a fixed zoom for now
this.tileZoom = INVALID_ZOOM;
		this.setPrefSize(1.0,1.0);
		this.setMinSize(1.0,1.0);
		this.setMaxSize(1.0,1.0);
	}
	
	public void setViewRegionManager(ViewRegionManager viewRegionManager) {
		this.viewRegionManager = viewRegionManager;
		viewRegionManager.addMapListener(this);
	}
	
	public void setTileInfo(TileInfo tileInfo) {
		this.tileInfo = tileInfo;
		if(tileInfo != null) {
this.getChildren().clear();
			minZoom = tileInfo.getMinZoom();
			maxZoom = tileInfo.getMaxZoom();
			pixelsPerTile = tileInfo.getTileSize();
			this.setVisible(true);
		}
		else {
this.getChildren().clear();
			minZoom = Integer.MIN_VALUE;
			maxZoom = Integer.MAX_VALUE;
			pixelsPerTile = 1;
			this.setVisible(false);
		}
	}
	
	//--------------------------
	// Layer Interface
	//--------------------------
	
	public void reset() {
		tileZoom = INVALID_ZOOM;
	}

	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onZoom(ViewRegionManager vrm) {		
		double pixelsPerMerc = viewRegionManager.getZoomScalePixelsPerMerc();
		setZoomScale(pixelsPerMerc);
		updateTiles();
	}
	
	private void setZoomScale(double pixelsPerMerc) {
		double tilesPerMerc = pixelsPerMerc / pixelsPerTile;
		int desiredScale = (int)Math.round(Math.log(tilesPerMerc)/Math.log(2));
	
		if(desiredScale > maxZoom) {
			tileZoom = maxZoom;
		}
		else if (desiredScale < minZoom) {
			tileZoom = minZoom;
		}
		else {
			tileZoom = desiredScale;
		}		
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {
		updateTiles();
	}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {
		updateTiles();
	}
	
	public void onLocalCoordinateSet(AffineTransform mercToLocal, Affine localToMercFX) {
	}
	
	//=================================
	// Private Methods
	//=================================
	
	private void updateTiles() {
		
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
		Affine mercToPixelsFX = viewRegionManager.getMercatorToPixelsFX();
		
		this.getTransforms().setAll(mercToPixelsFX,scaleCorrectionForTiles); 
		workingTiles.clear();
	}
	
	private void updateMercRange(Point2D point, AffineTransform pixelsToMerc, double[] mercRange) {
		pixelsToMerc.transform(point, point);
		if(point.getX() < mercRange[0]) mercRange[0] = point.getX();
		if(point.getY() < mercRange[1]) mercRange[1] = point.getY();
		if(point.getX() > mercRange[2]) mercRange[2] = point.getX();
		if(point.getY() > mercRange[3]) mercRange[3] = point.getY();
	}
	
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
