# Termite Quick Start #

Termite is an editor for indoor map data in OpenStreetMap.

#### Disclaimers ####

**Termite is currently in early testing.** Any feedback is appreciated to improve it. And, patience may be required in using it.

**OpenStreetMap currently does not have a standard for making indoor maps.** Termite uses one of the proposed mapping standards. It is also designed so the underlying model used can be changed.

Please send input to sutter@intransix.com.

## Setting Up ##
You need the following to get started.

  * The Termite program
    * The Termite program requires the Java 6 JRE (the same as JOSM).
    * Included in the package are some needed configuration files. The contents of the zip file should be unzipped as is into a folder.

A windows Batch file is included to open the editor on a windows machine.

## Using Termite ##

### Open the Program ###

  * Open the Termite program
    * Contact me for more help if there are any problems.
  * Turn on a base map
    * Go to the menu Map > Base Map  OpenStreetMap to turn on a base map using standard Mapnik OSM tiles.
    * You should see you are in the middle of the desert in Nevada, USA, including one or more isolated buildings. (Don't lose these.)
  * Move the map around
    * Pan - options:
      * left click and drag the mouse
      * use the arrow keys
    * Zoom - options:
      * UI buttons on map
      * mouse scroll wheel
      * page up/page down keys
  * Download data - to Download data, click the mouse to start a selection. Click again to complete the selection. Then press the "Download Data" button to download the data.
    * Select the area around the displayed buildings. These should be sample data.
      * The top left panel of the editor is the content pane. It should display the map data including a list of the buildings and their levels.

![http://wiki.openstreetmap.org/w/images/d/da/OpenProgram.png](http://wiki.openstreetmap.org/w/images/d/da/OpenProgram.png)

**Image** This is a view of the program with data loaded.

### Exploring ###

  * Examine the Nanoscale building (We will modify this building below. Do not commit these changes.)
    * Select level 1 of the nanoscale building from the map content tree.
    * Editor mode - The mode is selected using the buttons on the left of the tool bar. Go to Select Mode. Mode choices:
      * Select Mode
      * Node Tool
      * Way Tool
      * Geocode Mode
    * Hover the mouse on a wall between two rooms. The wall or possible a node should highlight, depending on where the mouse is.
    * While the mouse is hovering, press the PERIOD key. This should change the object that is highlighted. By pressing the PERIOD and COMMA keys you cycle through the options for what is highlighted. When you press the left mouse button, the highlighted object becomes selected.
      * The UI needs to be fixed - you won't see the object is highlighted until you move the mouse away.
      * The application must have focus for the keyboard input to work.
      * Since there are many overlapping objects, this will be important.

![http://wiki.openstreetmap.org/w/images/9/9c/NanoscaleHover.png](http://wiki.openstreetmap.org/w/images/9/9c/NanoscaleHover.png)

**Image** This is the first level of the Nanoscale Building with the mouse hovering over a wall.

#### Drawing A Way ####

  * While still in Select Mode, make sure no items are selected. If one is, click the mouse when it is over nothing.
  * Select the Way Tool from the mode toolbar.
  * Select an object from the Feature Type Tree corresponding to one of the red triangles. When you draw, this is the object type that will be created.
    * By selecting the root (object) of the tree, an unspecified object is created.
  * As you move the mouse, highlight lines (pink) appear that let you snap the mouse to given objects. As with select, pressing the PERIOD or COMMA buttons change which object is used for snapping, including the option to select not snapping.
  * Click the mouse somewhere outside the building. This creates a node.
As you move the mouse, a blue line appears, this is the line that will be created when you click the mouse.
  * Pink highlight lines may also appear, as before, to allow you to snap to nodes, extensions, parallels and other locations of interest.
    * Using the pink snap lines, you should be able to make a perfect rectangle.
      * This may require using the PERIOD or COMMA key.
    * When you close a line the way ends. You can also end a way by doing the following:
      * Double click - this creates a point and ends the way
      * Press escape - This ends the way without creating a point
    * You can continue an existing line by selecting it in select mode and then entering the Way Tool. Doing this will draw to the end of the way.
      * You can draw to the start of the way by selecting the way and then selecting the first node in the way.

![http://wiki.openstreetmap.org/w/images/0/05/DrawASquare.png](http://wiki.openstreetmap.org/w/images/0/05/DrawASquare.png)

**Image** This is an image of a square being drawn. The blue dashed line is where the next segment will be drawn. The pink dashed lines are snap guides. Note in the feature type panel the type "Wall" is selected.

#### Drawing a Node ####

  * The node tool is similar to the way tool, but is only draws a single node.
    * Currently no images are used for nodes. This will be added in the future.

#### Modifying Objects ####

  * When an object is selected, the properties panel shows the properties for the object. If a level or structure are active, those properties are also shown. Editting properties is done here.
  * You can select multiple points or ways using the shift key.
  * Some operations require you to select a node in a way (remove node, for example) To do this:
    * Select a way
    * Select a node in that way (no shift key is needed).
  * The buttons in the toolbar are actions that can be done when an object is selected: You can experiment with these actions.
    * Delete - Deletes whatever objects are selected
    * Remove Node - If a nodes in a way is selected, this will remove the ndoe from the way.
    * Change Feature Type - This will update the selected object to by the feature type currently selected in the feature type panel.
    * Create Level - This creates a level for the given object. This is how indoor maps are created. (We will do this later.)
    * Move - If you press the move button you can move the selected objects.
      * As a shortcut, press the "m" key on the keyboard instead of pressing the UI button on the toolbar.
      * The move will end when you click the map or if you press esc.
      * Virtual nodes are shown on the map at the midpoint in each segment. To add a point to a segment, drag one of the virtual nodes.

![http://wiki.openstreetmap.org/w/images/d/de/Move.png](http://wiki.openstreetmap.org/w/images/d/de/Move.png)

**Image** This images shows a node being dragged, with the blue dashed lines previewing the edit. The pink dashed line is a snap guide for an extension of a wall.

#### Undo/Redo ####

  * You can use CTRL-Z and CTRL-Y to undo and redo.
    * NOTE - There can be stray highlights after an undo, such as the higlight for a selected object or one of the other highlights. These highlights may cover the object that was undone.
      * If some highlights remain after a undo/redo, go to select mode and clear the selected objects by clicking at an empty spot on the screen.

### Adding an Indoor Map ###

#### Loading the Image ####

  * Get a image for a floor plan. We will copy this image in the editor.
  * For now the image should be an overhead image, properly to scale.
  * Using one of the base maps (Mapquest OSM is recommended), navigate the map to the location of the building you want to enter.
    * The search feature is not implemented so you must manually pan there.
  * Download data for the location you want.
    * Since this is the dev DB, there is a good chance there will be no data there.
  * Import Source Image
    * Go to the menu Map > Source Images to open the source image dialog.
    * From the dialog, press the "New" button.
    * Open an image file.
    * Press close on the dialog.
    * The image should appear over the map.

#### Geocode the image ####

  * In the supplemental panel beneath the map, there is a "Map Layers" tab. From here you can modify the opacity of any of the layers. This will be helpful for geocoding and drawing.
  * Press the Geocode Mode button.
  * For geocoding, aerial imagery should be used. It is also possible to use a base map if the base map has the building outline in a good location.
    * For now select the Bing Aerial imagery for the base map.
  * The following geocode options are available:
    * 2 point - Translate, Scale and Rotate the image
    * 3 point Orthoganol - Translate, Scale X, Scale Y and Rotate the image. - NOT WORKING PROPERLY NOW!
    * 3 point Free (or 3 Point Affine) - NOT IMPLEMENTED!
    * If not already selected, select the 2 point geocode.
    * Press the toolbar button "Translate" OR, for a shortcut, press the 1 button.
    * Click the mouse on a point of the image. This is one anchor point.
    * Press the toolbar button "Rotate/Scale" OR, for ashortcut, press the 2 button.
    * Click the mouse on a point of the image to place another anchor point.
    * Click on the first anchor point and then press the "m" key (or the MOVE toolbar button). Click the mouse to place the image over the map..
    * Do the same for the second point to control the rotate and scale.
    * Repeat until the image is aligned as you want it.

![http://wiki.openstreetmap.org/w/images/6/68/Geocode.png](http://wiki.openstreetmap.org/w/images/6/68/Geocode.png)

**Image** This image shows a geocoded drawing. More detailed description of geocoding buildings should be provided. Because aerial imagery is often taken at an angle, matching the floor plan image to the roof can give an incorrect geocode. The image should be matched to where the building meets the ground.

#### Tracing the Image ####

  * When one or more images is loaded, a UI element appears next to the zoom in/zoom out buttons. This allows you to set the reference frame for the map display. By default the north is up. You can also select one of the images as a source frame. This makes tracing easier since most lines should be horizontal or vertical.
  * First a building outline is needed. This can be traced from the source imagery or derived another method using the way tool.
  * Once the building is drawn, go to Select mode and select the building.
  * Once the building is selected, press the "Create Level" button. A dialog will open. Enter the data.
    * ZLevel - A integer for the floor. Use 0 for the ground floor, negatives   for levels below ground and positives for levels above the ground floor.
      * NOTE - The zlevel may not match the name of the floor. THAT IS OKAY.
    * name - This is a string for the name of the floor. In the United States, often the ground floor is called "1". Use the value that appears on signage for the floor, preferably the value that appears on an elevator button.
  * When this is done a new structure and level list should appear in the Map content panel tree.
  * Select the new level from the tree. Any object drawn when this level is selected will appear on this level.
  * Trace the source imagery using the Select, Node and Way editor modes.
    * NOTE - There is currently not a good way of matching the level to the outline. This will be added in the future.

MORE DETAILS ON METHODS AND TAGGING ARE TO BE ADDED. Some links for more information:
  * http://wiki.openstreetmap.org/wiki/Indoor/Termite/Mapping_Recommendations

### Commit the Data ###

  * If you have data you would like to commit, go to the File menu and press "Commit".
    * You will be prompted for a login. This should be your login on the dev server.
    * You will be prompted for a commit message.
    * The data will then be uploaded.

# Appendix #

## UI Elements ##

  * Menu Bar - Holds the menus
  * Toolbar
    * Mode Toolbar - Hold buttons to select the edit mode
    * Submode Toolbar - Additional controls specific to the selected mode.
  * Left Panel
    * Content Tree - This lets you select the map area to view, which may be the outdoors or a building level.
    * Feature Type Panel - This lets you select the feature type. The feature type tells the type of object. For example, the feature type may be "highway=primary". See more on the feature type below.
    * Property Panel - This shows properties of the selected object. The properties can be edited from here.
  * Map Panel - This holds the map.
  * Supplemental Panel - This contains tabs with additional functionality.
    * Map Layers Tab - This tab lets you control the opacity of the different visible layers, which is useful when tracing objects.

![http://wiki.openstreetmap.org/w/images/2/21/Termite_UI_v0.png](http://wiki.openstreetmap.org/w/images/2/21/Termite_UI_v0.png)