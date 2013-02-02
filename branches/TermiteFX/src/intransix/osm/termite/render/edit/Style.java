package intransix.osm.termite.render.edit;

import java.awt.Color;
import java.awt.Stroke;

/**
 *
 * @author sutter
 */
public class Style {
	public Color color;
	public Stroke stroke;
	public float pointRadius;
	
	/** Constructor for points */
	public Style(Color color, Stroke stroke, float radius) {
		this.color = color;
		this.stroke = stroke;
		this.pointRadius = radius;
	}

}
