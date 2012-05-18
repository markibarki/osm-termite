package intransix.osm.termite.theme;

import java.awt.*;
import intransix.osm.termite.map.geom.Feature;

/**
 *
 * @author sutter
 */
public class Style {
	
	//////////TEMPORARY/////////////////
	private String namespace;
	private String name;
	
	public boolean matches(Feature feature) {
		String type = feature.getProperty(namespace);
		return ((type != null)&&(type.equalsIgnoreCase(name)));
	}
	//////////////////////////////////////
	
	private Color bodyColor;
	private Color outlineColor;
	private float outlineScale = 0;
	private float outlineWidth;
	private Stroke stroke = null;
	
	public Style(String namespace, String name, Color bodyColor, Color outlineColor, float outlineWidth) {
		this.namespace = namespace;
		this.name = name;
		this.bodyColor = bodyColor;
		this.outlineColor = outlineColor;
		this.outlineWidth = outlineWidth;
	}
	
	
	
	public Color getBodyColor() {
		return bodyColor;
	}
	
	public Color getOutlineColor() {
		return outlineColor;
	}
	
	public Stroke getStroke(float zoomScale) {
		if((outlineWidth == 0)||(outlineColor == null)) return null;
		
		if((outlineScale != zoomScale)||(stroke == null)) {
			outlineScale = zoomScale;
			stroke = new BasicStroke(outlineWidth/outlineScale);
		}
		return stroke;
	}
}
