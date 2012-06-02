package intransix.osm.termite.app.gui;

import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.net.URL;
import java.util.HashMap;
import java.awt.image.ImageObserver;

/**
 * This is a layer for a tile map source.
 * 
 * @author sutter
 */
public class TileLayer implements Layer, ImageObserver, MapListener {
	
	//=========================
	// Properties
	//=========================
	
	public final static int MAX_CACHE_SIZE = 50;
	public final static int INVALID_ZOOM = -1;
	
	private int zoom = INVALID_ZOOM;
	private HashMap<String,Tile> tileCache = new HashMap<String,Tile>();
	private MapPanel mapPanel;
	private String urlTemplate;	
	private int maxZoom;
	private int tileSize;

	//=========================
	// Public Methods
	//=========================
	
	public TileLayer(String urlTemplate, int maxZoom, int tileSize) {
		this.urlTemplate = urlTemplate;
		this.maxZoom = maxZoom;
		this.tileSize = tileSize; 
	}
	
	//--------------------------
	// Layer Interface
	//--------------------------
	
	@Override
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mapToPixels = mapPanel.getMapToPixels();
		AffineTransform pixelsToMap = mapPanel.getPixelsToMap();
		Rectangle visibleRect = mapPanel.getVisibleRect();
		
		//we need to select the zoom
		if(zoom == INVALID_ZOOM) {
			onZoom(mapPanel.getZoomScale());
		}
		
		int activeZoom = this.zoom;
		
		//get tile range
		int[] tileRange = {Integer.MAX_VALUE,Integer.MAX_VALUE,0,0};
		//get the needed range of tiles
		updateRange(visibleRect.x,visibleRect.y,pixelsToMap,activeZoom,tileRange);
		updateRange(visibleRect.x+visibleRect.width,visibleRect.y,pixelsToMap,activeZoom,tileRange);
		updateRange(visibleRect.x+visibleRect.width,visibleRect.y+visibleRect.height,pixelsToMap,activeZoom,tileRange);
		updateRange(visibleRect.x,visibleRect.y+visibleRect.height,pixelsToMap,activeZoom,tileRange);
		
		//transform to mercator coordinates
		g2.transform(mapToPixels);
		
		long currentTime = System.currentTimeMillis();
		boolean tileRequested = false;
		int activeTileCount = 0;
		for(int ix = tileRange[0]; ix <= tileRange[2]; ix++) {
			for(int iy = tileRange[1]; iy <= tileRange[3]; iy++) {
				//make sure we don't try to plot too many tiles
				activeTileCount++;
				if(activeTileCount >= MAX_CACHE_SIZE-1) {
					return;
				}
					
				//add a tile to map
				String key = this.getUrl(ix, iy, activeZoom);
				Tile tile = getTile(key);
				if(tile != null) {
					tile.render(g2);
					tile.setActive(currentTime);
				}
				else if(!tileRequested) {
					requestTile(ix,iy,activeZoom,key);
					tileRequested = true;
				}
			}
		}
	}
	
	@Override
	public boolean imageUpdate(Image image, int infoflags, int x, int y, int width, int height) {
		if((infoflags & ImageObserver.ALLBITS) != 0) {
			//just do a repaint
			mapPanel.repaint();
			return false;
		}
		else {
			return true;
		}
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onZoom(double zoomScale) {
		int desiredScale = (int)Math.round(Math.log( zoomScale / tileSize)/Math.log(2) + MercatorCoordinates.MERCATOR_ZOOM);
		if(desiredScale > maxZoom) {
			zoom = maxZoom;
		}
		else {
			zoom = desiredScale;
		}
	}
	
	@Override
	public void onPanStart() {}
	
	@Override
	public void onPanEnd() {}
	
	//=================================
	// Protected Methods
	//=================================
	
	/** This method reads the URL from the template, using the inputs
	 * ix, iy and zoom in that order. If different values are needed for the
	 * string this method can be overridden. 
	 * 
	 * @param ix		The x tile index
	 * @param iy		The y tile index
	 * @param zoom		The zoom scale
	 * @return			The url string
	 */
	protected String getUrl(int ix, int iy, int zoom) {
		return String.format(urlTemplate,ix,iy,zoom);
	}
	
	
	//=================================
	// Private Methods
	//=================================
	
	/** This updates the range of tiles as a function of a input point in pixels. */
	private void updateRange(int pixX, int pixY, AffineTransform pixelsToMap, 
			int activeZoom, int[] tileRange) {
		
		//convert the point to mercator
		Point tileXY = new Point();
		Point2D test = new Point2D.Double();
		test.setLocation(pixX,pixY);
		pixelsToMap.transform(test, test);
		//get the tile this is on
		int div = 1 << (MercatorCoordinates.MERCATOR_ZOOM - activeZoom);
		int tileX = (int)(test.getX()/div);
		int tileY = (int)(test.getY()/div);
		//update the required range
		if(tileRange[0] > tileX) tileRange[0] = tileX;
		if(tileRange[1] > tileY) tileRange[1] = tileY;
		if(tileRange[2] < tileX) tileRange[2] = tileX;
		if(tileRange[3] < tileY) tileRange[3] = tileY;
	}
	
	/** This method loads a tile from the cache. If the tile is not
	 * present null is returned. The key should be the same as the url. 
	 * 
	 * @param key
	 * @return 
	 */
	private Tile getTile(String key) {
		return tileCache.get(key);
	}

	/** This method makes an asynchronous request for a tile. */
	private void requestTile(int x, int y, int zoom, String url) {
		Tile tile = new Tile(x,y,zoom,url);
		Image image = getImage(url);
		tile.setImage(image);
		if(tileCache.size() > MAX_CACHE_SIZE) {
			//get rid of the oldest tile
			long oldestLastActive = Long.MAX_VALUE;
			Tile oldestTile = null;
			for(Tile temp:tileCache.values()) {
				long lastActive = temp.getActiveTime();
				if(lastActive < oldestLastActive) {
					oldestLastActive = lastActive;
					oldestTile = temp;
				}
			}
			if(oldestTile != null) {
				tileCache.remove(oldestTile.getUrl());
			}	
		}
		tile.setActive(System.currentTimeMillis());
		tileCache.put(url,tile);
	}

	/** This method requests an image. */
	private Image getImage(String urlString) {
		try {
			URL url = new URL(urlString);
			Image image = java.awt.Toolkit.getDefaultToolkit().createImage(url);
			java.awt.Toolkit.getDefaultToolkit().prepareImage(image,-1,-1,this);
			return image;
		} 
		catch (Exception e) {
			return null;
		} 
	}
		
}
