package intransix.osm.termite.render.edit;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;



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
	
	private double DASH_SPACING_HOVER = 1.5;
	private double DASH_SPACING_PENDING = 3;
	
	public StyleInfo() {
		List<Double> hoverDashSpacing = new ArrayList<>();
		hoverDashSpacing.add(DASH_SPACING_HOVER);
		List<Double> pendingDashSpacing = new ArrayList<>();
		hoverDashSpacing.add(DASH_SPACING_PENDING);
		
		SELECT_STYLE = new Style(Color.RED, SELECT_WIDTH,SELECT_RADIUS);
		HOVER_SEGMENT_STYLE = new Style(Color.MAGENTA, HOVER_SEGMENT_WIDTH,PRESELECT_RADIUS);
		HOVER_EXTENSION_STYLE = new Style(Color.MAGENTA, HOVER_EXTENSION_WIDTH,PRESELECT_RADIUS,hoverDashSpacing);
		PENDING_STYLE = new Style(Color.BLUE, PENDING_WIDTH,PENDING_RADIUS,pendingDashSpacing);
	}
	
	public Style SELECT_STYLE;
	public Style HOVER_SEGMENT_STYLE;
	public Style HOVER_EXTENSION_STYLE;
	public Style PENDING_STYLE;
	
}
