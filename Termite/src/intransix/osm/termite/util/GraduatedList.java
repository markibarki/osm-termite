package intransix.osm.termite.util;

import java.util.*;

/**
 * This list holds the objects in a group of lists, ordered by a a discrete integer value.
 * 
 * @author sutter
 */
public class GraduatedList<T> {
	
	//=======================
	// Private Properties
	//=======================
	
	
	private List<IndexedList<T>> lists = new ArrayList<IndexedList<T>>();
	
	//=======================
	// Public Methods
	//=======================
	
	/** This method returns the list of lists. */
	public List<IndexedList<T>> getLists() {
		return lists;
	}
	
	/** This adds the given object from the specified list. */
	public void add(T obj, int order) {
		List<T> list = getList(order);
		list.add(obj);
	}
	
	/** This removes the given object from the specified list. */
	public void remove(T obj, int order) {
		List<T> list = getList(order);
		list.remove(obj);
	}
	
	/** This moves an object from one order list to another. */
	public void move(T obj, int toOrder, int fromOrder) {
		remove(obj,fromOrder);
		add(obj,toOrder);
	}
	
	//=======================
	// Private Methods
	//=======================
	
	/** This looks up the list with the given order. If one does not exits,
	 * a new one is created and added in the proper location. */
	private List<T> getList(int order) {
		int index = 0;
		for(IndexedList<T> list:lists) {
			//go until we find the index
			if(list.order < order) {
				index++;
				continue;
			}
			else {
				if(list.order == order) return list;
				else break;
			}
		}
		//list not found, add a new one
		IndexedList<T> list = new IndexedList<T>();
		lists.add(index,list);
		return list;
	}
	
	//=======================
	// Internal Classes
	//=======================
	
	/** This is a list that has an index integer on it. */
	public class IndexedList<F> extends ArrayList<F> {
		public int order;
	}
	
}
