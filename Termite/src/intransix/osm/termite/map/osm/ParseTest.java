package intransix.osm.termite.map.osm;

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

			DefaultHandler handler = new OsmXml();
			saxParser.parse("test0.xml", handler);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
