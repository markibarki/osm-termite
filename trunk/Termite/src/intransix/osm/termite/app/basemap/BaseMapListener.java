package intransix.osm.termite.app.basemap;

import intransix.osm.termite.render.tile.TileInfo;


/**
 *
 * @author sutter
 */
public interface BaseMapListener {
	
	/** This method is called when the baseMap changes. */
	void baseMapChanged(TileInfo tileInfo);
	
}
