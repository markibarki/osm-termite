package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.prop.FeatureInfo;
import intransix.osm.termite.theme.Style;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;

/**
 *
 * @author sutter
 */
public class FeatureLevelGeom implements Comparable<FeatureLevelGeom> {
	
	private TermiteFeature feature;
	private TermiteLevel level;
	private Shape shape;

	/** renders the feature. */
	public void render(Graphics2D g) {
		Style style = feature.getStyle();
		if((shape != null)&&(style != null)) {
			
			//load style params
			Color fillColor = null;
			Color strokeColor = null;
			Stroke stroke = null;
			if(feature.getIsArea()) {
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
				g.fill(shape);
			}
			if((strokeColor != null)&&(stroke != null)) {
				g.setStroke(stroke);
				g.setColor(strokeColor);
				g.draw(shape);
			}
			
			
		}
	}
	
	@Override
	public int compareTo(FeatureLevelGeom other) {
		FeatureInfo otherFeatureInfo = other.feature.getFeatureInfo();
		FeatureInfo thisFeatureInfo = this.feature.getFeatureInfo();
		int otherZorder;
		int thisZorder;
		if(otherFeatureInfo == null) {
			otherZorder = FeatureInfo.DEFAULT_ZORDER;
		}
		else {
			otherZorder = otherFeatureInfo.getZorder();
		}
		if(thisFeatureInfo == null) {
			thisZorder = FeatureInfo.DEFAULT_ZORDER;
		}
		else {
			thisZorder = thisFeatureInfo.getZorder();
		}
		return (thisZorder - otherZorder);	
	}
}
