package intransix.osm.termite.render.tile;

import intransix.osm.termite.map.osm.OsmModel;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.*;

/**
 *
 * @author sutter
 */
public class Tile {
	
	private int x;
	private int y;
	private int zoom;
	private String url;
	private long activeTime = 0;
	private Image image;
	
	public Tile(int x, int y, int zoom, String url) {
		this.x = x;
		this.y = y;
		this.zoom = zoom;
		this.url = url;
	}
	
	public void render(Graphics2D g2) {
		int mult = 1 << (MercatorCoordinates.MERCATOR_ZOOM - zoom);
		int xPix = (int)(mult * x - OsmModel.mxOffset);
		int yPix = (int)(mult * y - OsmModel.myOffset);
		
		g2.drawImage(image,xPix,yPix, mult, mult, null);
		
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
