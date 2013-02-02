package intransix.osm.termite.render.edit;

import java.awt.BasicStroke;
import java.awt.Color;

/**
 *
 * @author sutter
 */
public class StyleInfo { 
	private float SELECT_WIDTH = 1;
	private float SELECT_RADIUS = 3;
	private float HOVER_SEGMENT_WIDTH = 2;
	private float HOVER_EXTENSION_WIDTH = 2;
	private float PRESELECT_RADIUS = 5;
	private float PENDING_WIDTH = 1;
	private float PENDING_RADIUS = 3;
	private float MITER_LIMIT = 3;
	
	private float[] DASH_SPACING_HOVER = {1.5f};
	private float DASH_PHASE_HOVER = 0;
	private float[] DASH_SPACING_PENDING = {3};
	private float DASH_PHASE_PENDING = 0;
	
	private BasicStroke SELECT_STROKE = new BasicStroke(SELECT_WIDTH);
	private BasicStroke HOVER_SEGMENT_STROKE = new BasicStroke(HOVER_SEGMENT_WIDTH);
	private BasicStroke HOVER_EXTENSION_STROKE = new BasicStroke(HOVER_EXTENSION_WIDTH, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, MITER_LIMIT,DASH_SPACING_HOVER,DASH_PHASE_HOVER);
	private BasicStroke PENDING_STROKE = new BasicStroke(PENDING_WIDTH, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, MITER_LIMIT,DASH_SPACING_PENDING,DASH_PHASE_PENDING);
	
	public Style SELECT_STYLE = new Style(Color.RED, SELECT_STROKE,SELECT_RADIUS);
	public Style HOVER_SEGMENT_STYLE = new Style(Color.MAGENTA, HOVER_SEGMENT_STROKE,PRESELECT_RADIUS);
	public Style HOVER_EXTENSION_STYLE = new Style(Color.MAGENTA, HOVER_EXTENSION_STROKE,PRESELECT_RADIUS);
	public Style PENDING_STYLE = new Style(Color.BLUE, PENDING_STROKE,PENDING_RADIUS);
	
}
