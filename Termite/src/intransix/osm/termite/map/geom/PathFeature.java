package intransix.osm.termite.map.geom;

import java.awt.*;
import java.awt.geom.*;
import intransix.osm.termite.theme.Style;

/**
 * This represents a line or area feature.
 * 
 * @author sutter
 */
public class PathFeature extends Feature {
	
	private Path2D path;
	
	public PathFeature(FeatureType featureType) {
		super(featureType);
	}
	
	public Path2D getPath() {
		return path;
	}
	
	public void setPath(Path2D path) {
		this.path = path;
	}
	
	public void render(Graphics2D g) {
		Style style = this.getStyle();
		if((path != null)&&(style != null)) {
			
			//load style params
			Color fillColor = null;
			Color strokeColor = null;
			Stroke stroke = null;
			FeatureType ft = getFeatureType();
			if((ft == FeatureType.AREA)||(ft == FeatureType.MULTIAREA)) {
				fillColor = style.getBodyColor();
				strokeColor = style.getOutlineColor();
			}
			if((ft == FeatureType.LINE)||(ft == FeatureType.MULTILINE)) {
				fillColor = null;
				strokeColor = style.getBodyColor();
			}
			stroke = style.getStroke(1);
			
			//render the object	
			if(fillColor != null) {
				g.setPaint(fillColor);
				g.fill(path);
			}
			if((strokeColor != null)&&(stroke != null)) {
				g.setStroke(stroke);
				g.setColor(strokeColor);
				g.draw(path);
			}
			
			
		}
	}
	
}
