package intransix.osm.termite.app.mapdata.instruction;

/**
 * This is an exception thrown by an instruction if the instruction fails
 * but leaves the data unchanged. This state of the data can be recovered by
 * undoing the previous actions in the instruction. If an instruction throws an
 * exception after changing the state, then the data can not be recovered and
 * we have an non-recoverable exception.
 * 
 * @author sutter
 */
public class UnchangedException extends Exception {
	public UnchangedException(String msg) {
		super(msg);
	}
}
