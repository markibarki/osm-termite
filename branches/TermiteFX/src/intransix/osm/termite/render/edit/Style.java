package intransix.osm.termite.render.edit;

import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;


/**
 *
 * @author sutter
 */
public class Style {
	public Color color;
	public double strokeWidth;
	public double pointRadius;
	public List<Double> dashArray;
	
	/** Constructor for points */
	public Style(Color color, double strokeWidth, double pointRadius) {
		this.color = color;
		this.strokeWidth = strokeWidth;
		this.pointRadius = pointRadius;
	}
	
	public Style(Color color, double strokeWidth, double pointRadius, List<Double> dashArray) {
		this.color = color;
		this.strokeWidth = strokeWidth;
		this.pointRadius = pointRadius;
		this.dashArray = dashArray;
	} 
	
	public double getStrokeWidth() {
		return strokeWidth;
	}
	
	public double getPointRadius() {
		return pointRadius;
	}
	
	public void setLineStyle(Shape shape) {
		shape.setStroke(color);
		if(dashArray != null) {
			shape.getStrokeDashArray().addAll(dashArray);
		}
	}
	
	public void setAreaStyle(Shape shape) {
		shape.setFill(color);
	}

}
