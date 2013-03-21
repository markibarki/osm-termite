/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.edit;

/**
 *
 * @author sutter
 */
public interface ShapeGraphic {
	
	void setStyle(Style style, double pixelsToMerc);
	
	void setPixelsToMerc(double pixelsToMerc);
	
}
