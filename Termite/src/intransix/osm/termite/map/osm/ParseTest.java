package intransix.osm.termite.map.osm;

import java.util.Collection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class ParseTest extends DefaultHandler {
 
  public void parse() {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			OsmXml osmXml = new OsmXml();
			saxParser.parse("test.xml", osmXml);
			
			Collection<OsmObject> osmObjects = osmXml.getOsmObjects();
			
			//create the termite objects
			for(OsmObject osmObject:osmObjects) {
				//create table of termite objects
				//-structures
				//-levels
				//-features (indoor and outdoor; multi, way and node)
				//-ways
				//-nodes
				//(I think that is it)
				
				//create termite object maps, by type
				//-structure
				//-level
				//-feature
				//-way
				//-node
			}
			
			//make sure termite geom is marked as dirty
			//plot a level, this will construct the java shape and other display info

		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
