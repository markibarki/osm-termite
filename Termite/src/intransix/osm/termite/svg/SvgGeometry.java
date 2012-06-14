package intransix.osm.termite.svg;

import intransix.osm.termite.map.feature.FeatureInfoMap;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.HashMap;

import org.apache.batik.dom.svg.*;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class SvgGeometry {
//
//	//====================================
//	// Fields
//	//====================================
//	public String id;
//	public String fill;
//	public String stroke;
//	public double strokeWidth;
//	public Shape shape;
//	
//	//====================================
//	// Public Functions
//	//====================================
//	
//	public SvgGeometry(SVGGraphicsElement svgObjectElement, Shape shape) {
//		
//		this.shape = shape;
//		
//		//get id
//		id = svgObjectElement.getAttribute(SVGConstants.SVG_ID_ATTRIBUTE);
//		
//		//get style properties
//		HashMap<String,String> styleTable = null;
//		String style = svgObjectElement.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
//		if((style != null)&&(style.length() > 0)) {
//			styleTable = getStyleTable(style);
//		}
//		
//		String fillString = svgObjectElement.getAttribute(SVGConstants.SVG_FILL_ATTRIBUTE);
//		if(((fillString == null)||(fillString.length() == 0))&&(styleTable != null)) {
//			fillString = styleTable.get(CSSConstants.CSS_FILL_PROPERTY);
//		}
//		if((fillString == null)||(fillString.length() == 0)) fillString = null;
//		
//		if(fillString != null) {
//			fill = formatColor(fillString);
//		}
//		else {
//			fill = null;
//		}
//		
//		String strokeString = svgObjectElement.getAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE);
//		if(((strokeString == null)||(strokeString.length() == 0))&&(styleTable != null)) {
//			strokeString = styleTable.get(CSSConstants.CSS_STROKE_PROPERTY);
//		}
//		if((strokeString == null)||(strokeString.length() == 0)) strokeString = null;
//		
//		if(strokeString != null) {
//			stroke = formatColor(strokeString);
//		}
//		else {
//			stroke = null;
//		}
//		
//		String widthString = svgObjectElement.getAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE);
//		if(((widthString == null)||(widthString.length() == 0))&&(styleTable != null)) {
//			widthString = styleTable.get(CSSConstants.CSS_STROKE_WIDTH_PROPERTY);
//		}
//		if((widthString == null)||(widthString.length() == 0)) widthString = null;
//		
//		if(widthString != null) {
//			widthString = widthString.replace("px", "");
//			strokeWidth = Double.parseDouble(widthString);
//		}
//		else {
//			strokeWidth = 0;
//		}
//	}
//	
//	public SvgGeometry(TermiteFeature feature, FeatureInfoMap featureInfoMap) {
//		FeatureInfo featureInfo = featureInfoMap.getFeatureInfo(feature);
//		String inputColor = null;
//		if(featureInfo != null) {
//			inputColor= featureInfo.getInputColor();
//		}
//		
////get a better way to handle this
//if(inputColor == null) inputColor = FeatureInfo.DEFAULT_IMPORT_FILL;
//
//		boolean isArea;
//		if(feature instanceof PathFeature) {
//			//get the shape
//			this.shape = ((PathFeature)feature).getPath();
//			isArea = ((PathFeature)feature).getIsArea();
//		}
//		else if(feature instanceof PointFeature) {
//			isArea = true;
//			
//			//create a shape for the point
//			Point2D point = ((PointFeature)feature).getPoint();
//			Ellipse2D ellipse = new Ellipse2D.Double(point.getX(),point.getY(),
//					FeatureInfo.DEFAULT_POINT_RADIUS,FeatureInfo.DEFAULT_POINT_RADIUS);
//			this.shape = ellipse;
//		}
//		else {
////get a better way to handle this
//throw new RuntimeException("Unrecognized feature type");
//		}
//		
//		//set the id
//		this.id = feature.getId();
//			
//		//set the color
//		if(isArea) {
//			this.fill = inputColor;
//			this.stroke = FeatureInfo.DEFAULT_EXPORT_STROKE;
//			this.strokeWidth = FeatureInfo.DEFAULT_EXPORT_STROKE_WIDTH;
//		}
//		else {
//			this.fill = null;
//			this.stroke = inputColor;
//			this.strokeWidth = FeatureInfo.DEFAULT_EXPORT_STROKE_WIDTH;
//		}
//	}
//	
//	//import functions/method
//	// - get id
//	// - lookup object
//	// - pass mapper and object (if not available)
//	// - update or create object
//
//	//====================================
//	// Private Functions
//	//====================================
//	
//	private String formatColor(String colorString) {
//		if(colorString.equals("none")) return null;
//		return colorString;
//	}
//	
//	private static HashMap<String,String> getStyleTable(String style) {
//		HashMap<String,String> styleTable = new HashMap<String,String>();
//		String[] pairs = style.split(";");
//		String[] keyValue;
//		for(String pair:pairs) {
//			keyValue = pair.split(":");
//			if(keyValue.length != 2) {
//				throw new RuntimeException("Invalid style string format");
//			}
//			styleTable.put(keyValue[0].trim(), keyValue[1].trim());
//		}
//		return styleTable;
//	}
}

