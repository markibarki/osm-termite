package intransix.osm.termite.theme;

import intransix.osm.termite.map.DataParser;
import intransix.osm.termite.map.KeyNode;
import intransix.osm.termite.map.PropertyNode;
import java.awt.*;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class Style {
	
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
	
	private Style() {
	}
	
	public Color getBodyColor() {
		return bodyColor;
	}
	
	public Color getOutlineColor() {
		return outlineColor;
	}
	
	public Stroke getStroke(float zoomScale) {
		if((outlineWidth == 0)||(outlineColor == null)) return null;
		
		if((outlineScale != zoomScale)||(stroke == null)) {
			outlineScale = zoomScale;
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
		

}
	
/** This is a data parser that keeps track of key and value together and 
* passes uses the the most recent for default values for the child object.
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

