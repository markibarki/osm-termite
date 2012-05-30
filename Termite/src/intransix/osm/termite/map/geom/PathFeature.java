package intransix.osm.termite.map.geom;

import java.awt.*;
import java.awt.geom.*;
import intransix.osm.termite.theme.Style;

/**
 * This represents a line or area feature.
 * 
 * @author sutter
 */
public class PathFeature extends TermiteFeature {
	
	private Path2D path;
	private boolean isArea = false;
	
	public PathFeature(boolean isArea) {
		this.isArea = isArea;
	}
	
	public PathFeature() {
	}
	
	public void setIsArea(boolean isArea) {
		this.isArea = isArea;
	}
	
	public boolean getIsArea() {
			return isArea;
	}
	
	public Path2D getPath() {
		return path;
	}
	
	public void setPath(Path2D path) {
		this.path = path;
	}
	
	public void updatePath(Path2D path, boolean isArea) {
//for now just replace
		this.path = path;
		this.isArea = isArea;
	}
	
	public void render(Graphics2D g) {
		Style style = this.getStyle();
		if((path != null)&&(style != null)) {
			
			//load style params
			Color fillColor = null;
			Color strokeColor = null;
			Stroke stroke = null;
			if(isArea) {
				fillColor = style.getBodyColor();
				strokeColor = style.getOutlineColor();
			}
			else {
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
