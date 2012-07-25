package intransix.osm.termite.map.data;

import java.util.*;

/**
 * <p>All edits to data should be done using an EditAction. An edit action includes
 * a set of instructions, corresponding to changes to the data. The actions should
 * be run in the UI thread, making all data changes synchronized in this thread.
 * Actions can also be undone, providing a mechanism for undo/redo functionality.</p>
 * 
 * <p>The action will return true if it succeeds. If an exception is thrown the
 * data may be corrupt and the program should exit without saving any data.</p>
 * 
 * <p>Each action execution (or undo) will be given an edit number. Objects which
 * are updated have their data version set to this edit number to allow visibility
 * as to when data for an object changes. When data on an object updated, the 
 * object as well as any object that contains it (a way contains nodes and a relation
 * contains nodes, ways or relations) has the version updated. The version number
 * update is not cascaded beyond this.</p>

 * @author sutter
 */
public class EditAction {
	
	//========================
	// Properties
	//========================
	
	private OsmData osmData;
	private String desc;
	private List<EditInstruction> instructions = new ArrayList<EditInstruction>();
	private boolean hasBeenExecuted = false;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor */
	public EditAction(OsmData osmData, String desc) {
		this.osmData = osmData;
		this.desc = desc;
	}
	
	/** This method sets the description. One place this is used is in the UI
	 * for undo and redo actions, to prompt the user as to what the undo/redo
	 * will do.
	 * 
	 * @param desc 
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	/** This retrieves the description for the action. */
	public String getDesc() {
		return desc;
	}
	
	/** This method adds an instruction to the action. */
	public void addInstruction(EditInstruction instr) {
		instructions.add(instr);
	}
	
	/** This method returns the OsmData. */
	public OsmData getOsmData() {
		return osmData;
	}
	
	/** This method executes the action. It should be done in the UI thread. */
	public boolean doAction() throws Exception {
		int editNumber = osmData.getNextEditNumber();
		
		int index = 0;
		int cnt = instructions.size();
		EditInstruction instr;
		try {
			for(index = 0; index < cnt; index++) {
				instr = instructions.get(index);
				instr.doInstruction(osmData,editNumber);
			}
		}
		catch(UnchangedException ex) {
			//we can fix one unchanged exception
			//undo any actions to this point
			//starting with the previous
			for(index--; index >= 0; index--) {
				instr = instructions.get(index);
				instr.undoInstruction(osmData,editNumber);
			}
			return false;
		}
		
		//notify data changed
		if(!hasBeenExecuted) {
			osmData.saveAction(this);
			hasBeenExecuted = true;
		}
		osmData.dataChanged(editNumber);
		
		return true;
	}
	
	/** This method undoes the action. It should be done in the UI thread. It should
	 * also be done only after the action has been done the first time. 
	 * 
	 * @return
	 * @throws Exception 
	 */
	public boolean undoAction() throws Exception {
		int editNumber = osmData.getNextEditNumber();
		
		int index = 0;
		int cnt = instructions.size();
		EditInstruction instr;
		try {
			for(index = cnt - 1; index >= 0; index--) {
				instr = instructions.get(index);
				instr.undoInstruction(osmData,editNumber);
			}
			
		}
		catch(UnchangedException ex) {
			//we can fix one unchanged exception
			//undo any actions to this point
			//starting with the previous
			for(index++; index < cnt; index++) {
				instr = instructions.get(index);
				instr.doInstruction(osmData,editNumber);
			}
			return false;
		}
		
		//notify data changed
		osmData.dataChanged(editNumber);

		return true;
	}
}
