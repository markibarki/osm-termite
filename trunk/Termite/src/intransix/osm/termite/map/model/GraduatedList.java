package intransix.osm.termite.map.model;

import java.util.*;

/**
 * This list holds the objects in a group of lists, ordered by a a discrete integer value.
 * 
 * @author sutter
 */
public class GraduatedList<T> {
	
	private List<IndexedList<T>> lists = new ArrayList<IndexedList<T>>();
	
	/** This method returns the list of lists. */
	public List<IndexedList<T>> getLists() {
		return lists;
	}
	
	/** This adds the given object from the specified list. */
	public void add(T obj, int zorder) {
		List<T> list = getList(zorder);
		list.add(obj);
	}
	
	/** This removes the given object from the specified list. */
	public void remove(T obj, int zorder) {
		List<T> list = getList(zorder);
		list.remove(obj);
	}
	
	/** This moves an object from one zorder list to another. */
	public void move(T obj, int toZorder, int fromZorder) {
		remove(obj,fromZorder);
		add(obj,toZorder);
	}
	
	/** This looks up the list with the given zorder. If one does not exits,
	 * a new one is created and added in the proper location. */
	private List<T> getList(int zorder) {
		int index = 0;
		for(IndexedList<T> list:lists) {
			//go until we find the index
			if(list.zorder < zorder) {
				index++;
				continue;
			}
			else {
				if(list.zorder == zorder) return list;
				else break;
			}
		}
		//list not found, add a new one
		IndexedList<T> list = new IndexedList<T>();
		lists.add(index,list);
		return list;
	}
	
	/** This is a list that has an index integer on it. */
	public class IndexedList<F> extends ArrayList<F> {
		public int zorder;
	}
	
}
