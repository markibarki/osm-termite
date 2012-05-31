package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;

/**
 *
 * @author sutter
 */
public class TermiteObject extends MapObject {
	
	//==============================
	// Properties
	//==============================
	
	public final static long INVALID_ID = 0;
	
	private long id;
	private boolean isVirtual = false;
	private boolean isLoaded = false;
	
	//==============================
	// Public Methods
	//==============================
	
	public boolean getIsLoaded() {
		return isLoaded;
	}
	
	public void isLoasded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
	
	public long getId() {
		return id;
	}
	
	public boolean getIsVirtual() {
		return isVirtual;
	}
	
	public void setIsVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}
	
	//==============================
	// Package Methods
	//==============================
	
	/** This method creates an object with the specified ID.
	 * 
	 * @param id			The id of the object
	 */
	TermiteObject(long id) {
		this.id = id;
	}
	
}
