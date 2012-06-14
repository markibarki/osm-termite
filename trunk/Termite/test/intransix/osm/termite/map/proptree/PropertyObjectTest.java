package intransix.osm.termite.map.proptree;

import intransix.osm.termite.map.proptree.PropertyNode;
import intransix.osm.termite.map.proptree.KeyNode;
import intransix.osm.termite.map.proptree.DataParser;
import intransix.osm.termite.map.osm.OsmObject;
import intransix.osm.termite.map.osm.OsmNode;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;

/**
 * Things to test:
 * 
 * Parse
 * a) parse json with multiple levels of keys and values, with no data
 * b) same as 1 but with data on keys and values
 * 0.1) check name of parsed data 
 * 
 * Lookup Data or Lookup ClassifyingObject
 * 1.1) classify object that matches a key (in root) and a value on the key
 * 1.2) classify object that matches a key (in root) but not a value on key
 * 1.3) classify an object that does not match any keys (in root)
 * 
 * Lookup Key
 * 2.1) lookup key that exists in root
 * 2.2) lookup key that exists outside of root.
 * 2.3) lookup a key that exists outside of root but map object does not have a matching property
 * 
 * Lookup Key Value
 * 3.1, 3.2, 3.3) Do all the test cases in lookup key with a valid value
 * 3.4, 3.5, 3.6) Do all the test cases in lookup key with an invalid value
 * 
 * @author sutter
 */
public class PropertyObjectTest {
	
	public PropertyObjectTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void propTest1() {
		//create the json
		String jsonString = "{"
				+ "  'name':'theme',"
				+ "  'keys':["
				+ "    {'name':'buildingpart',"
				+ "      'data':{'fill':'red','stroke':'blue'},"
				+ "  	   'values':["
				+ "  	     {'name':'room',"
				+ "          'data':{'fill':'red','stroke':'white'},"
				+ "          'keys':[{"
				+ "            'name':'bathroom',"
				+ "            'values':["
				+ "              {'name':'yes',"
				+ "                'keys':["
				+ "                  {'name':'gender',"
				+ "	                   'values':[{'name':'male'},{'name':'female'},{'name':'unisex'}]"
				+ "                  }"
				+ "                ]"
				+ "			     }]"
				+ "          }]"
				+ "        },"
				+ "        {'name':'stairs','data':{'fill':'red','stroke':null}},"
				+ "        {'name':'hallway','data':{'fill':'red','stroke':'white'}},"
				+ "        {'name':'unit','data':{'fill':'red','stroke':'white'}},"
				+ "        {'name':'wall','data':{'fill':'red','stroke':'white'}},"
				+ "        {'name':'escalator',"
				+ "          'data':{'fill':'red','stroke':'white'},"
				+ "  	       'keys':["
				+ "            {'name':'direction',"
				+ "              'values':["
				+ "                {'name':'up'},"
				+ "                {'name':'down'},"
				+ "                {'name':'both'},"
				+ "                {'name':'arriving'}"
				+ "              ]"
				+ "            }"
				+ "          ]"
				+ "        }"
				+ "	     ]"
				+ "    },"
				+ "    {'name':'furnishing',"
				+ "       'data':{'fill':'gray','stroke':'black'},"
				+ "  		'values':[{'name':'shelf'},{'name':'chair'},{'name':'table'}]"
				+ "    }"
				+ "  ]"
				+ "}";
		try {
			
			//no data, single level keys
			JSONObject json = new JSONObject(jsonString);
			
			//parse with no data
			PropertyNode<Object,Object> pond = new PropertyNode<Object,Object>();
			pond.parse(json, null, null);
			
			//parse with data
			PropertyNode<StyleData,StyleData> po = new PropertyNode<StyleData,StyleData>();
			po.parse(json, null, new TestDataParser());
			
			// TEST 0.1
			//name should be theme
			assertTrue(po.getName().equalsIgnoreCase("theme"));
			assertTrue(pond.getName().equalsIgnoreCase("theme"));
			
			OsmObject mapObject;
			PropertyNode out;
			
			//use a node as the generic map object
			mapObject = new OsmNode();
			
			StyleData st;
			KeyNode<StyleData,StyleData> key;
			PropertyNode<StyleData,StyleData> prop;
			
			//no properties in map object, should return the root object
			
			//TEST 1.3) classify an object that does not match any keys (in root)
			st = po.getPropertyData(mapObject);
			assertTrue(st.matches("dark gray","dark black"));
			//TEST 2.1) lookup key that exists in root
			key = po.getKey(mapObject,"buildingpart");
			assertTrue(keyMatches(key,"buildingpart"));
			//TEST 2.3) lookup a key that exists outside of root but map object 
			//does not have a matching property
			key = po.getKey(mapObject,"gender");
			assertTrue(keyMatches(key,null));
			//TEST 3.1) lookup key that exists in root with valid value
			prop = po.getKeyValue(mapObject,"buildingpart","room");
			assertTrue(propertyMatches(prop,"room"));
			//TEST 3.3) lookup a key that exists outside of root but map object 
			//does not have a matching property, with valid value
			prop = po.getKeyValue(mapObject,"gender","male");
			assertTrue(propertyMatches(prop,null));
			//TEST 3.4) lookup key that exists in root with invalid value
			prop = po.getKeyValue(mapObject,"buildingpart","duck");
			assertTrue(propertyMatches(prop,null));
			//TEST 3.6) lookup a key that exists outside of root but map object 
			//does not have a matching property, with invalid value
			prop = po.getKeyValue(mapObject,"gender","android");
			assertTrue(propertyMatches(prop,null));
			
			//add a property to the map object that doesn't exist
			mapObject.setProperty("buildingpart","elbow");
			
			//TEST 1.2) classify object that matches a key (in root) but not a value on key
			st = po.getPropertyData(mapObject);
			assertTrue(st.matches("dark gray","dark black"));

			//add a valid property
			mapObject.setProperty("buildingpart","room");
			
			//TEST 1.1) classify object that matches a key (in root) and a value on the key
			st = po.getPropertyData(mapObject);
			assertTrue(st.matches("red","white"));
			//TEST 2.2) lookup key that exists outside of root.
			key = po.getKey(mapObject,"bathroom");
			assertTrue(keyMatches(key,"bathroom"));
			//TEST 3.2) lookup a key that exists outside of root with a valid value 
			prop = po.getKeyValue(mapObject,"bathroom","yes");
			assertTrue(propertyMatches(prop,"yes"));
			//TEST 3.5) lookup key that exists in root with an invalid value
			prop = po.getKeyValue(mapObject,"bathroom","maybe");
			assertTrue(propertyMatches(prop,null));
			
			//additional tests
			mapObject.setProperty("bathroom","yes");
			mapObject.setProperty("gender","male");
			
			//returns male data - but this is the same as room - not much of a test
			prop = po.getClassifyingProperty(mapObject);
			assertTrue(propertyMatches(prop,"male"));
			
			//lookup gender - should work
			key = po.getKey(mapObject,"gender");
			assertTrue(keyMatches(key,"gender"));
			
			//lookup gender male - should work
			prop = po.getKeyValue(mapObject,"gender","male");
			assertTrue(propertyMatches(prop,"male"));
			
			//lookup gender female - should work because we don't exclude 
			//values for which we have a different value
			//(but we might want to not allow it if a different value exists already)
			prop = po.getKeyValue(mapObject,"gender","female");
			assertTrue(propertyMatches(prop,"female"));
			
			//lookup furnishing - should work because we allow multiple keys
			//(but this is a key we might want to make exclusive with "buildingpart")
			key = po.getKey(mapObject,"furnishing");
			assertTrue(keyMatches(key,"furnishing"));
			
			System.out.println("test done");
			
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
			//flag an unexpected exception
			assertTrue(false);
		}
	}
	
	private boolean keyMatches(KeyNode key, String name) {
		if(name != null) {
			return key.getName().equalsIgnoreCase(name);
		}
		else {
			return key == null;
		}
	}
	
	private boolean propertyMatches(PropertyNode prop, String name) {
		if(name != null) {
			return prop.getName().equalsIgnoreCase(name);
		}
		else {
			return prop == null;
		}
	}
	
	/** This creates a Style object that parses two fields from a json with
	 * a default to a parent value. */
	private static class StyleData {
		String fill;
		String stroke;
		
		public static StyleData parse(JSONObject json, StyleData parent) {
			StyleData st = new StyleData();
			String defaultFill = null;
			String defaultStroke = null;
			if(parent != null) {
				defaultFill = parent.fill;
				defaultStroke = parent.stroke;
			}
			else {
				defaultFill = "dark gray";
				defaultStroke = "dark black";
			}
			if(json != null) {
				st.fill = json.optString("fill",defaultFill);
				st.stroke = json.optString("stroke",defaultStroke);
			}
			else {
				st.fill = defaultFill;
				st.stroke = defaultStroke;
			}
			return st;
		}
		
		public boolean matches(String fill, String stroke) {
			boolean fillMatch = (fill != null) ? fill.equalsIgnoreCase(this.fill) : this.fill == null;
			boolean strokeMatch = (stroke != null) ? stroke.equalsIgnoreCase(this.stroke) : this.stroke == null;
			return fillMatch && strokeMatch;
		}
	}
	
	/** This is a data parser that keeps track of key and value together and 
	 * passes uses the the most recent for default values for the child object.
	 */
	private static class TestDataParser extends DataParser<StyleData,StyleData> {
		
		@Override
		public StyleData parseValueData(JSONObject json, KeyNode<StyleData,StyleData> parentKey) {
			StyleData parentData = null;
			if(parentKey != null) {
				parentData = parentKey.getData();
			}
			return parseData(json,parentData);
		}
	
		@Override
		public StyleData parseKeyData(JSONObject json, PropertyNode<StyleData,StyleData> parentValue) {
			StyleData parentData = null;
			if(parentValue != null) {
				parentData = parentValue.getData();
			}
			return parseData(json,parentData);
		}
		
		private StyleData parseData(JSONObject json, StyleData parent) {
			JSONObject dataJson = json.optJSONObject("data");
			return StyleData.parse(dataJson, parent);
		}
		
	}
}
