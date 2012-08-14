package intransix.osm.termite.render.edit;

import java.awt.BasicStroke;
import java.awt.Color;

/**
 *
 * @author sutter
 */
public class StyleInfo {
	public double RADIUS_PIXELS = 3; 
	public float SELECT_WIDTH = 1;
	public float HOVER_PRESELECT_WIDTH = 1;
	public float HOVER_EXTENSION_WIDTH = 1;
	public float HOVER_OTHER_WIDTH = 1;
	public float PENDING_WIDTH = 1;
	public float MITER_LIMIT = 5f;
	
	public Color SELECT_COLOR = Color.RED;
	public Color HOVER_PRESELECT_COLOR = Color.MAGENTA;
	public Color HOVER_OTHER_COLOR = Color.PINK;
	public Color PENDING_COLOR = Color.BLUE;
	
	public float[] DASH_SPACING = {3f};
	public float DASH_PHASE = 0f;
	
	public BasicStroke SELECT_STROKE = new BasicStroke(SELECT_WIDTH);
	public BasicStroke HOVER_PRESELECT_STROKE = new BasicStroke(HOVER_PRESELECT_WIDTH);
	public BasicStroke HOVER_EXTENSION_STROKE = new BasicStroke(HOVER_EXTENSION_WIDTH, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, MITER_LIMIT,DASH_SPACING,DASH_PHASE);
	public BasicStroke HOVER_OTHER_STROKE = new BasicStroke(HOVER_OTHER_WIDTH);
	public BasicStroke PENDING_MOVE_STROKE = new BasicStroke(PENDING_WIDTH);
	public BasicStroke PENDING_CREATE_STROKE = new BasicStroke(PENDING_WIDTH, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, MITER_LIMIT,DASH_SPACING,DASH_PHASE);
	
}
