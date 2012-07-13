package intransix.osm.termite.render.edit;

import java.awt.geom.Point2D;
import java.util.HashMap;
import intransix.osm.termite.map.data.OsmObject;
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
		
		/** This returns the precedence order for this snap type. */
		public final boolean getEnabled() {
			return enabled;
		}
		
		/** This updates the precedence order for this snap type. */
		public final void setEnabled(boolean enabled) {
			this.enabled = enabled;
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
	
	@Override
	public int compareTo(SnapObject obj) {
		//order the snap objects first based on the order of the snap operation
		int order = this.snapType.getOrder() - obj.snapType.getOrder();
		if(order != 0) return order;
		
		//if that is equal, choose the smaller error
		return (int)Math.signum(this.err2 - obj.err2);
	}
	
	/** This method looks up an edit object for this snap object. 
	 * 
	 * @return			The edit object
	 */
	public abstract EditObject getSelectEditObject();
	
}
