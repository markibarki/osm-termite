package intransix.osm.termite.theme;

import intransix.osm.termite.map.proptree.DataParser;
import intransix.osm.termite.map.proptree.KeyNode;
import intransix.osm.termite.map.proptree.PropertyNode;
import java.awt.*;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 * This object is a style for rendering an map object.
 * @author sutter
 */
public class Style {
	
	//===============
	// Properties
	//===============
	
	public final static Color DEFAULT_BODY_COLOR = Color.LIGHT_GRAY;
	public final static Color DEFAULT_OUTLINE_COLOR = Color.DARK_GRAY;
	public final static float DEFAULT_OUTLINE_WIDTH = 2;
	
	//persistent fields
	private Color bodyColor = DEFAULT_BODY_COLOR;
	private Color outlineColor = DEFAULT_OUTLINE_COLOR;
	private float outlineWidth = DEFAULT_OUTLINE_WIDTH;
	
	//working fields
	private float outlineScale = 0;
	private Stroke stroke = null;
	
	//===============
	// Public Methods
	//===============
	
	/** This is the body color for the object. For an area, this corresponds to 
	 * the fill. For a line, this corresponds to the stroke used on the line. */
	public Color getBodyColor() {
		return bodyColor;
	}
	
	/** This is the outline color for an object. For an area, it should be used for 
	 * the stroke. For a line, it is not used unless the line includes an outline 
	 * outside the stroke.
	 * 
	 * @return 
	 */
	public Color getOutlineColor() {
		return outlineColor;
	}
	
	/** This method gets a stroke object. It will print the stroke in a fixed pixel
	 * width.
	 * 
	 * @param zoomScale		The magnification that will be down from the drawn object to
	 *						pixels. This is used so the pixel width comes out correctly.
	 * @return				A Stroke object
	 */
	public Stroke getStroke(double zoomScale) {
		if((outlineWidth == 0)||(outlineColor == null)) return null;
		
		if((outlineScale != zoomScale)||(stroke == null)) {
			outlineScale = (float)zoomScale;
			stroke = new BasicStroke(outlineWidth/outlineScale);
		}
		return stroke;
	}
	
		
	/** This method returns a style object. The JSON can be null. If so, a default 
	 * style is returned. The parent argument is optional and is used as default values
	 * if it is present. Otherwise global defaults are used. */
	public static Style parse(JSONObject json, Style parent) {
		Style st = new Style();
		if(json != null) {
			//load body color
			String bodyString = json.optString("body",null);
			if(bodyString != null) {
				st.bodyColor = Color.decode(bodyString);
			}
			else if(parent != null) {
				st.bodyColor = parent.bodyColor;
			}
			
			//load outline color
			String outlineString = json.optString("outline",null);
			if(outlineString != null) {
				st.outlineColor = Color.decode(outlineString);
			}
			else if(parent != null) {
				st.outlineColor = parent.outlineColor;
			}
			
			//load the outline width
			float defaultOutlineWidth = (parent != null) ? parent.outlineWidth : DEFAULT_OUTLINE_WIDTH;
			st.outlineWidth = (float)json.optDouble("width",defaultOutlineWidth);
		}
		else if(parent != null) {
			st.bodyColor = parent.bodyColor;
			st.outlineColor = parent.outlineColor;
			st.outlineWidth = parent.outlineWidth;
		}

		return st;
	}
	
	//=================
	// Private Methods
	//=================
	private Style() {
	}
		

}
	
/** This is a data parser to parse the styles from a theme property tree.
 * It keeps track of key and value together and 
* uses the the most recent for default values for the child object.
*/
class StyleParser extends DataParser<Style,Style> {
	
	@Override
	public Style parseValueData(JSONObject json, KeyNode<Style,Style> parentKey) {
		Style parentData = null;
		if(parentKey != null) {
			parentData = parentKey.getData();
		}
		return parseData(json,parentData);
	}

	@Override
	public Style parseKeyData(JSONObject json, PropertyNode<Style,Style> parentValue) {
		Style parentData = null;
		if(parentValue != null) {
			parentData = parentValue.getData();
		}
		return parseData(json,parentData);
	}

	private Style parseData(JSONObject json, Style parent) {
		JSONObject dataJson = json.optJSONObject("style");
		return Style.parse(dataJson, parent);
	}
}

