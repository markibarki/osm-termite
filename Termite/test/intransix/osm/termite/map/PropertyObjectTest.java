/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map;

import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import org.json.JSONArray;

/**
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
				+ "            'keys':["
				+ "              {'name':'gender',"
				+ "	               'values':[{'name':'male'},{'name':'female'},{'name':'unisex'}]"
				+ "              }"
				+ "            ]"
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
			
			PropertyObject<Object> po = new PropertyObject<Object>();
			po.parse(json, null, null);
			
			//name should be theme
			assertTrue(po.getName().equalsIgnoreCase("theme"));
			
			MapObject mapObject;
			PropertyObject out;
			
			mapObject = new MapObject();
			
			//PROPERTY LOOKUP
			
			//no properties in map object, should return the root object
			out = po.getPropertyObject(mapObject);
			assertTrue(out.getName().equalsIgnoreCase("theme"));
			
			mapObject.setProperty("buildingpart","unit");
			//returns unit object
			out = po.getPropertyObject(mapObject);
			assertTrue(out.getName().equalsIgnoreCase("unit"));
			
			mapObject.setProperty("buildingpart","room");
			//returns room object
			out = po.getPropertyObject(mapObject);
			assertTrue(out.getName().equalsIgnoreCase("room"));
			
//THIS RETURNS THE BATHROOM. I THINK KEY OR PROPERTY IS RETURNED. IS THIS OK?
			mapObject.setProperty("bathroom","yes");
			//returns room property - no match inside bathroom key
			out = po.getPropertyObject(mapObject);
			assertTrue(out.getName().equalsIgnoreCase("room"));
			
			mapObject.setProperty("gender","male");
			//returs male property
			out = po.getPropertyObject(mapObject);
			assertTrue(out.getName().equalsIgnoreCase("male"));
			
			//KEY LOOKUP
			KeyObject key;
			
			//should get buildingpart key
			key = po.getKeyObject(mapObject,"buildingpart");
			assertTrue(key.getName().equalsIgnoreCase("buildingpart"));
			
//THIS FINDS THE KEY ANYWAY. FIX THAT.
			//direction key exists but is not on the path for this object
			key = po.getKeyObject(mapObject,"direction");
			assertTrue(key == null);
			
			//bathroom key is on path
			key = po.getKeyObject(mapObject,"bathroom");
			assertTrue(key.getName().equalsIgnoreCase("bathroom"));
			
			System.out.println("test done");
			
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
			//flag an unexpected exception
			assertTrue(false);
		}
	}
}
