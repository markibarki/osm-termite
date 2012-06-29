package intransix.osm.termite.map.osm;

/**
 *
 * @author sutter
 */
public class UpdateInstruction<T extends OsmObject> extends EditInstruction {
	
	private T osmObject;
	private EditData<T> targetData;
	private EditData<T> initialData;
	
	public UpdateInstruction(T objectToUpdate, EditData<T> targetData) {
		this.osmObject = objectToUpdate;
		this.targetData = targetData;
	}
	@Override
	public void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		initialData = executeUpdate(osmData,osmObject,targetData,editNumber);
	}
	
	@Override
	public void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		targetData = executeUpdate(osmData,osmObject,initialData,editNumber);
	}
}
