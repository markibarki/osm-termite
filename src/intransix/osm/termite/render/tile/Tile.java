package intransix.osm.termite.render.tile;


import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
	private Point2D workingPoint = new Point2D.Double();
	
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
		
		this.image = new Image(url,true);
		
		this.setImage(image);
	}
	
	public void setLocation(AffineTransform mercToTileLayerTransform, double mercToTileLayerScale) {
		workingPoint.setLocation(mercX,mercY);
		mercToTileLayerTransform.transform(workingPoint, workingPoint);
		this.setX(workingPoint.getX());
		this.setY(workingPoint.getY());
		this.setFitHeight(mercHeight * mercToTileLayerScale);
		this.setFitWidth(mercWidth * mercToTileLayerScale);
System.out.println("Tile " + this.tileX + "," + this.tileY + ": " + workingPoint.getX() + "," + workingPoint.getY());
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
