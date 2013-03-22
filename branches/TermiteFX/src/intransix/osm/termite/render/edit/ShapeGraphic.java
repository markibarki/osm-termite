package intransix.osm.termite.render.edit;

/**
 * This is the base class for a shape plotted in a local scale.
 * 
 * @author sutter
 */
public interface ShapeGraphic {

	/** This method sets the style for rendering. */
	void setStyle(Style style, double pixelsToLocalScale);
	
	/** This method updates the pixel to local transform, used to scale the pixel width parameters. */
	void setPixelsToLocalScale(double pixelsToLocalScale);
	
}
