package intransix.osm.termite.app.edit;

import javafx.scene.input.MouseEvent;
import java.awt.geom.Point2D;

/**
 * This is the base class for mouse move actions. 
 * @author sutter
 */
public interface MouseMoveAction {
	
	/** This is called when the action is loaded on the layer. */
	public boolean init();
	
	/** This is called when the mouse id moved. */
	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e);
}
