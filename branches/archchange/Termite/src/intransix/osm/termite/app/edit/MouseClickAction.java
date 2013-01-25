package intransix.osm.termite.app.edit;

import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;

/**
 * This is the base class for edit mouse click actions. 
 * 
 * @author sutter
 */
public interface MouseClickAction {
	
	/** This will be called when the mouse click is first loaded to the layer. */
	public boolean init();
	
	/* This method is called when the mouse is pressed. */
	public void mousePressed(Point2D mouseMerc, MouseEvent e);
}
