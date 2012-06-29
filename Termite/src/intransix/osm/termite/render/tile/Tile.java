package intransix.osm.termite.render.tile;

import intransix.osm.termite.map.data.OsmModel;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public class Tile {
	
	private int x;
	private int y;
	private String url;
	private long activeTime = 0;
	private Image image;
	private AffineTransform tileToMerc;
	
	public Tile(int x, int y, int zoom, int numPixels, String url) {
		this.x = x;
		this.y = y;
		this.url = url;
		
		double tileToMercScale = 1.0 / (1 << zoom);
		tileToMerc = new AffineTransform();
		tileToMerc.scale(tileToMercScale,tileToMercScale);
		tileToMerc.translate(x,y);
		tileToMerc.scale(1.0/numPixels,1.0/numPixels);
		
	}
	
	public void render(Graphics2D g2) {
		java.awt.geom.AffineTransform orig = g2.getTransform();
		g2.transform(tileToMerc);
		g2.drawImage(image,0,0,null);
		g2.setTransform(orig);
	}
	
	public String getUrl() {
		return url;
	}
	
	public long getActiveTime() {
		return activeTime;
	}
	
	public void setActive(long activeTime) {
		this.activeTime = activeTime;
	}
	
	public void setImage(Image image) {
		this.image = image;
	}
}
