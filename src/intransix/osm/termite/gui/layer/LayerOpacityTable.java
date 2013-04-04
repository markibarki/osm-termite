/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.layer;


import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerListener;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author sutter
 */
public class LayerOpacityTable extends TableView implements MapLayerListener {
	
//	private final ObservableList<LayerOpacityTable.LayerData> data = FXCollections.observableArrayList();
private final ObservableList<MapLayer> data = FXCollections.observableArrayList();
	
	public LayerOpacityTable() {		
	}
	
	/** This method is called when the map layer list changes. */
	@Override
	public void layerListChanged(List<MapLayer> mapLayerList) {
		data.setAll(mapLayerList);
	}
		
    public void init() {

        this.setEditable(true);
	
        Callback<TableColumn, TableCell> textCellFactory =
             new Callback<TableColumn, TableCell>() {
                 public TableCell call(TableColumn p) {
                    return new LayerOpacityTable.TextEditingCell();
                 }
             };
		
		Callback<TableColumn, TableCell> doubleCellFactory =
             new Callback<TableColumn, TableCell>() {
                 public TableCell call(TableColumn p) {
                    return new LayerOpacityTable.OpacityEditingCell();
                 }
             };
 
        TableColumn firstNameCol = new TableColumn("Layer");
        firstNameCol.setMinWidth(100);
		firstNameCol.setSortable(false);
		firstNameCol.setCellValueFactory(new PropertyValueFactory<MapLayer, String>("name"));
		firstNameCol.setCellFactory(textCellFactory);
		firstNameCol.setOnEditCommit(
			new EventHandler<TableColumn.CellEditEvent<MapLayer, String>>() {
				@Override
				public void handle(TableColumn.CellEditEvent<MapLayer, String> t) {
					((MapLayer) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setName(t.getNewValue());
				}		
			}				
		);
 
        TableColumn lastNameCol = new TableColumn("Opacity");
        lastNameCol.setMinWidth(100);
		lastNameCol.setPrefWidth(200);
		lastNameCol.setSortable(false);
		lastNameCol.setCellValueFactory(new PropertyValueFactory<MapLayer, Double>("opacity"));
		lastNameCol.setCellFactory(doubleCellFactory);
		lastNameCol.setOnEditCommit(
			new EventHandler<TableColumn.CellEditEvent<MapLayer, Double>>() {
				@Override
				public void handle(TableColumn.CellEditEvent<MapLayer, Double> t) {
					((MapLayer) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setOpacity(t.getNewValue());
				}
			}
		);
 
        this.setItems(data);
        this.getColumns().addAll(firstNameCol, lastNameCol);
    }
 
	
	 class TextEditingCell extends TableCell<MapLayer, String> {
 
        private TextField textField;
 
        public TextEditingCell() {
        }
 
        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }
 
        @Override
        public void cancelEdit() {
            super.cancelEdit();
 
            setText((String) getItem());
            setGraphic(null);
        }
 
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
 
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }
 
        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.textProperty().addListener(new ChangeListener<String>(){
                @Override
                public void changed(ObservableValue<? extends String> arg0, 
						String oldValue, String newValue) {
					if (newValue != null) {
						commitEdit(newValue);

					}
                }
            });
        }
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
	 }
	 
	class OpacityEditingCell extends TableCell<MapLayer, Double> {
 
        private Slider slider;
 
        public OpacityEditingCell() {
			createSlider();
        }
 
        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
 
            if (empty) {
                setText(null);
                setGraphic(null);
            } 
			else {
				slider.setValue(getItem());
				setText(null);
				setGraphic(slider);
            }
        }
 
        private void createSlider() {
			slider = new Slider();
			slider.setMin(0);
			slider.setMax(1.0);
			
            slider.valueProperty().addListener(new ChangeListener<Number>(){
                @Override
                public void changed(ObservableValue<? extends Number> arg0, 
						Number oldValue, Number newValue) {
				
					if(newValue != null) {
						TableView<MapLayer> tv = tableViewProperty().get();
						TableRow<MapLayer> tr = tableRowProperty().get();

					   tv.getItems().get(tr.getIndex()).setOpacity(newValue.doubleValue());
					}
                }
            });
			
        }
    }

}
