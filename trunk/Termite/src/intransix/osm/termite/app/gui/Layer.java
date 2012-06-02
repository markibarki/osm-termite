/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app.gui;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public interface Layer {
	
	public void setMapPanel(MapPanel mapPanel);
	
	public void render(Graphics2D g2);
}
