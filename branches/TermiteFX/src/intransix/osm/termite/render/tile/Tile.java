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
		this.zoom = zoom;
		this.url = url;
		
		double tileToMercScale = 1.0 / (1 << zoom);
		this.mercX = this.tileX * tileToMercScale;
		this.mercY = this.tileY * tileToMercScale;
		this.mercWidth = tileToMercScale;
		this.mercHeight = tileToMercScale;
		
		//Javafx seems to round the image locations even though we are using doubles.
		//To fix this we are using a large (power of two) integer multiplier
		this.setX(mercX * TileLayer.MERC_MULTIPLIER_SCALE);
		this.setY(mercY * TileLayer.MERC_MULTIPLIER_SCALE);
		this.setFitHeight(mercHeight * TileLayer.MERC_MULTIPLIER_SCALE);
		this.setFitWidth(mercWidth * TileLayer.MERC_MULTIPLIER_SCALE);
		
		this.image = new Image(url,true);
		
		this.setImage(image);
	}
	
	public long getActiveTime() {
		return activeTime;
	}
	
	public void setActive(long activeTime) {
		this.activeTime = activeTime;
	}
	
	public String getUrl() {
		return url;
	}
}
