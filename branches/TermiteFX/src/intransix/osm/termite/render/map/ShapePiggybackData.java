/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.map;

import intransix.osm.termite.map.workingdata.PiggybackData;
import javafx.scene.shape.Shape;

/**
 *
 * @author sutter
 */
public class ShapePiggybackData extends PiggybackData {
	private Shape shape;
	
	public Shape getShape() {
		return shape;
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
}
