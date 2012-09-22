package intransix.osm.termite.app.edit.snapobject;

import intransix.osm.termite.app.edit.EditDrawable;
import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;
import java.awt.geom.Point2D;
/**
 *
 * @author sutter
 */
public abstract class SnapObject extends EditDrawable implements Comparable<SnapObject> {
	
	/** This indicates the type of snap that is being done. */
	public enum SnapType {
		
		//enum values
		NODE(true,1),
		VIRTUAL_NODE(true,2),
		WAY(true,3),
		INTERSECTION(true,4),
		SEGMENT_INT(true,5),
		SEGMENT_EXT(true,6),
		SEGMENT_PERP(true,7),
		HORIZONTAL(true,8),
		VERTICAL(true,9),
		UNKNOWN(true,10);
		
		//this indicates the precedence order for this snap type
		private int order;
		private boolean enabled;
		
		/** Constructor. */
		SnapType(boolean enabled, int order) {
			this.enabled = enabled;
			this.order = order;
		}
		
		/** This returns the precedence order for this snap type. */
		public final int getOrder() {
			return order;
		}
		
		/** This updates the precedence order for this snap type. */
		public final void setOrder(int order) {
			this.order = order;
		}
	}
	
	public Point2D snapPoint;
	public double err2;
	public SnapType snapType;
	
	SnapObject(SnapType snapType) {
		this.snapType = snapType;
	}
	
	public void setSnapType(SnapType snapType) {
		this.snapType = snapType;
	}
	
	/** This gets the render style for hovering. */
	public Style getHoverStyle(StyleInfo styleInfo) {
		switch(snapType) {
			case NODE:
			case VIRTUAL_NODE:
			case WAY:
			case INTERSECTION:
			case SEGMENT_INT:
				return styleInfo.HOVER_SEGMENT_STYLE;

			case SEGMENT_EXT:
			case SEGMENT_PERP:
			case HORIZONTAL:
			case VERTICAL:
				return styleInfo.HOVER_EXTENSION_STYLE;

			default:
				return styleInfo.HOVER_SEGMENT_STYLE;
		}
	}
	
	@Override
	/** This is used to order the snap object in a hover so the most important comes first. */
	public int compareTo(SnapObject obj) {
		//order the snap objects first based on the order of the snap operation
		int order = this.snapType.getOrder() - obj.snapType.getOrder();
		if(order != 0) return order;
		
		//if that is equal, choose the smaller tiebreaker value
		return (int)Math.signum(this.compareTiebreaker() - obj.compareTiebreaker());
	}
	
	/** This value is used by compareTo to differentiate matching object types. */
	protected double compareTiebreaker() {
		return this.err2;
	}
	
	/** This method looks up an select object for this snap object. 
	 * 
	 * @return			The edit object
	 */
	public abstract Object getSelectObject();
	
}
