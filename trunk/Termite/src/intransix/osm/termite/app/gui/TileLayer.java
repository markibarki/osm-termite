package intransix.osm.termite.app.gui;

import intransix.osm.termite.map.geom.TermiteLevel;
import intransix.osm.termite.map.geom.TermiteFeature;
import intransix.osm.termite.map.geom.TermiteStructure;
import intransix.osm.termite.map.geom.TermiteData;
import intransix.osm.termite.map.geom.FeatureLevelGeom;
import intransix.osm.termite.theme.Theme;
import intransix.osm.termite.util.MercatorCoordinates;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import org.json.JSONObject;
import java.awt.image.ImageObserver;

/**
 *
 * @author sutter
 */
public class TileLayer implements Layer, ImageObserver {
	
	public final static String URL_TEMPLATE = "http://otile1.mqcdn.com/tiles/1.0.0/osm/%1d/%2d/%3d.jpg";
	public final static int MAX_ZOOM = 18;
	public final static int MAX_CACHE_SIZE = 50;
	
	private HashMap<String,Tile> tileCache = new HashMap<String,Tile>();
	private MapPanel mapPanel;
		
	public void onZoom(double zoomScale) {}
	public void onPanStart() {}
	public void onPanEnd() {}
	
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mapToPixels = mapPanel.getMapToPixels();
		AffineTransform pixelsToMap = mapPanel.getPixelsToMap();
		Rectangle visibleRect = mapPanel.getVisibleRect();
		
		int zoom = MAX_ZOOM;
		
		//get tile range
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = 0;
		int maxY = 0;
	
		Point tileXY = new Point();
		Point2D test = new Point2D.Double();
		test.setLocation(visibleRect.x,visibleRect.y);
		pixelsToMap.transform(test, test);
		getTileXY(tileXY, test.getX(), test.getY(), zoom);
		if(minX > tileXY.x) minX = tileXY.x;
		if(minY > tileXY.y) minY = tileXY.y;
		if(maxX < tileXY.x) maxX = tileXY.x;
		if(maxY < tileXY.y) maxY = tileXY.y;
		test.setLocation(visibleRect.x + visibleRect.width,visibleRect.y);
		pixelsToMap.transform(test, test);
		getTileXY(tileXY, test.getX(), test.getY(), zoom);
		if(minX > tileXY.x) minX = tileXY.x;
		if(minY > tileXY.y) minY = tileXY.y;
		if(maxX < tileXY.x) maxX = tileXY.x;
		if(maxY < tileXY.y) maxY = tileXY.y;
		test.setLocation(visibleRect.x + visibleRect.width,visibleRect.y + visibleRect.height);
		pixelsToMap.transform(test, test);
		getTileXY(tileXY, test.getX(), test.getY(), zoom);
		if(minX > tileXY.x) minX = tileXY.x;
		if(minY > tileXY.y) minY = tileXY.y;
		if(maxX < tileXY.x) maxX = tileXY.x;
		if(maxY < tileXY.y) maxY = tileXY.y;
		test.setLocation(visibleRect.x,visibleRect.y + visibleRect.height);
		pixelsToMap.transform(test, test);
		getTileXY(tileXY, test.getX(), test.getY(), zoom);
		if(minX > tileXY.x) minX = tileXY.x;
		if(minY > tileXY.y) minY = tileXY.y;
		if(maxX < tileXY.x) maxX = tileXY.x;
		if(maxY < tileXY.y) maxY = tileXY.y;
		
		g2.transform(mapToPixels);
		
		long currentTime = System.currentTimeMillis();
		boolean tileRequested = false;
		for(int ix = minX; ix <= maxX; ix++) {
			for(int iy = minY; iy <= maxY; iy++) {
				String key = this.getUrl(ix, iy, zoom);
				Tile tile = getTile(key);
				if(tile != null) {
					tile.render(g2);
					tile.setActive(currentTime);
				}
				else if(!tileRequested) {
					requestTile(ix,iy,zoom,key);
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
	
	
	//=================================
	// Private Methods
	//=================================
	
	String getUrl(int ix, int iy, int zoom) {
		return String.format(URL_TEMPLATE,zoom,ix,iy);
	}
	
	/** This method returns the tile coordinates for a given point. */
	void getTileXY(Point tileXY, double mx, double my, int zoom) {
		int div = 1 << (MercatorCoordinates.MERCATOR_ZOOM - zoom);
		tileXY.x = (int)mx/div;
		tileXY.y = (int)my/div;
	}
	
	private Tile getTile(String key) {
		return tileCache.get(key);
	}

	private void requestTile(int x, int y, int zoom, String url) {
		Tile tile = new Tile(x,y,zoom,url);
		Image image = getImage(url);
		tile.setImage(image);
		tileCache.put(url,tile);
		if(tileCache.size() > MAX_CACHE_SIZE) {
			//get rid of the oldest tile
			long oldestAge = Long.MAX_VALUE;
			Tile oldestTile = null;
			for(Tile temp:tileCache.values()) {
				long age = temp.getAge();
				if(age < oldestAge) {
					oldestAge = age;
					oldestTile = tile;
				}
			}
			if(oldestTile != null) {
				tileCache.remove(oldestTile.getUrl());
			}	
		}
	}

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
