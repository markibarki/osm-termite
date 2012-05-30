package intransix.osm.termite.svg;

import intransix.osm.termite.map.geom.*;
import java.io.IOException;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import intransix.osm.termite.map.prop.FeatureInfoMap;
import java.awt.geom.*;
import java.awt.*;
import java.io.PrintStream;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;

/**
 * This method converts between a map level and an SVG document
 * @author sutter
 */
public class SvgConverter {
	
	public void loadSvg(TermiteLevel level, String fileName, FeatureInfoMap featureInfoMap) {
		Document doc = loadXmlDoc(fileName);
		
		SvgDocument svgDocument = new SvgDocument();
		svgDocument.load(doc);
		
////////////////////////////////////////////////////////////
//this is just for testing
		
		TermiteStructure structure = level.getStructure();
		structure.setBounds(svgDocument.getDocSize());
		
		for(SvgGeometry geom:svgDocument.getObjectList()) {
			
			//get the object color
			String fillColor = geom.fill;
			String strokeColor = geom.stroke;
			String inputColor = null;
			boolean isArea;
			if(fillColor != null) {
				inputColor = fillColor;
				isArea = true;
			}
			else if(strokeColor != null) {
				inputColor = strokeColor;
				isArea = false;
			}
			else {
				//do something here - exception?
				continue;
			}
			
			//check if feature exists
			TermiteFeature feature = level.getFeature(geom.id);		
			
			//update the shape
			Shape shape = geom.shape;
			if(shape instanceof Path2D) {
				if(feature != null) {
if(feature instanceof PointFeature) {
	throw new RuntimeException("Switching from Node to Way not currently supported!");
}
				}
				else {
					feature = new PathFeature();
					level.addFeature(feature);
				}
				
				//update path
				((PathFeature)feature).updatePath((Path2D)shape,isArea);
			}
			if(shape instanceof Ellipse2D) {
				if(feature != null) {
if(feature instanceof PathFeature) {
	throw new RuntimeException("Switching from Way to Node not currently supported!");
}
				}
				else {
					feature = new PointFeature();
					level.addFeature(feature);
				}
				
				//create point from circle
				double x = ((Ellipse2D)shape).getCenterX();
				double y = ((Ellipse2D)shape).getCenterY();
				Point2D point = new Point2D.Double(x,y);
				
				//update point
				((PointFeature)feature).updatePoint(point);
			}
			
			//update the properties
			featureInfoMap.updateFeatureProperties(feature,inputColor);
		}
		
		//order the feature since we updated the list
		//we need to figure out a good way of doing this
		level.orderFeatures();
		
////////////////////////////////////////////////////////////
	}
	
	public void createSvg(TermiteLevel level, String fileName, FeatureInfoMap featureInfoMap) {
		//create xml doc
		SvgDocument svgDocument = new SvgDocument();
		
		TermiteStructure structure = level.getStructure();
		svgDocument.setDocSize(structure.getBounds());
		for(TermiteFeature feature:level.getFeatures()) {
			SvgGeometry geom = new SvgGeometry(feature,featureInfoMap);
			svgDocument.addGeometry(geom);
		}
		
		Document doc = svgDocument.create();
		
		//write the document
		writeXmlDoc(doc,fileName);
	}


	//==========================
	// Private Methods
	//==========================
	
	/** This method loads the SVG document. */
	private Document loadXmlDoc(String fileName) {
		try {
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		    return f.createDocument(fileName);
		    
		} catch (IOException ex) {
		    ex.printStackTrace();
		    return null;
		}
	}
	
	// This method writes a DOM document to a file
    private boolean writeXmlDoc(Document doc, String fileName) {
        try {     	
        	doc.normalizeDocument();
        	
            // Prepare the DOM document for writing
            DOMSource source = new DOMSource(doc);
    
            // Prepare the output file        
            PrintStream outStream = new PrintStream(fileName);
            Result result = new StreamResult(outStream);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
			outStream.flush();
			outStream.close();
			return true;
        } catch (Exception e) {
        	e.printStackTrace();
			return false;
        }
    }
	
}
