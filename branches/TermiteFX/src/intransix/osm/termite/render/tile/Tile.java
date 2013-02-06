package intransix.osm.termite.render.tile;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author sutter
 */
public class Tile extends ImageView {
	
	private int tileX;
	private int tileY;
	private int zoom;
	private double mercX;
	private double mercY;
	private double mercWidth;
	private double mercHeight;
	private String url;
	private long activeTime = 0;
	private Image image;
	
	public Tile(int x, int y, int zoom, int numPixels, String url) {
		this.tileX = x;
		this.tileY = y;
		this.url = url;
		
		double tileToMercScale = 1.0 / (1 << zoom);
		this.mercX = this.tileX * tileToMercScale;
		this.mercY = this.tileY * tileToMercScale;
		this.mercWidth = tileToMercScale;
		this.mercHeight = tileToMercScale;
		
		this.setX(mercX);
		this.setY(mercY);
		this.setFitHeight(mercHeight);
		this.setFitWidth(mercWidth);
		
		this.image = new Image(url,true);
	}
	
	public long getActiveTime() {
		return activeTime;
	}
	
	public void setActive(long activeTime) {
		this.activeTime = activeTime;
	}
}
