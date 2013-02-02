/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.layer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author sutter
 */
public class LayerOpacityTable extends TableView {
	
	private final ObservableList<LayerOpacityTable.LayerData> data =
	FXCollections.observableArrayList(
		new LayerOpacityTable.LayerData("Jacob", .4),
		new LayerOpacityTable.LayerData("Isabella", .6),
		new LayerOpacityTable.LayerData("Ethan",.3),
		new LayerOpacityTable.LayerData("Emma",.7),
		new LayerOpacityTable.LayerData("Michael",1.0)
	);
	
	public LayerOpacityTable() {		
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
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<LayerOpacityTable.LayerData, String>("layerName"));
		firstNameCol.setCellFactory(textCellFactory);
		firstNameCol.setOnEditCommit(
			new EventHandler<TableColumn.CellEditEvent<LayerOpacityTable.LayerData, String>>() {
				@Override
				public void handle(TableColumn.CellEditEvent<LayerOpacityTable.LayerData, String> t) {
					((LayerOpacityTable.LayerData) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setLayerName(t.getNewValue());
				}
			}
		);
 
        TableColumn lastNameCol = new TableColumn("Opacity");
        lastNameCol.setMinWidth(100);
		lastNameCol.setPrefWidth(200);
		lastNameCol.setSortable(false);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<LayerOpacityTable.LayerData, Double>("opacity"));
		lastNameCol.setCellFactory(doubleCellFactory);
		lastNameCol.setOnEditCommit(
			new EventHandler<TableColumn.CellEditEvent<LayerOpacityTable.LayerData, Double>>() {
				@Override
				public void handle(TableColumn.CellEditEvent<LayerOpacityTable.LayerData, Double> t) {
					((LayerOpacityTable.LayerData) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setOpacity(t.getNewValue());
				}
			}
		);
 
        this.setItems(data);
        this.getColumns().addAll(firstNameCol, lastNameCol);
    }
 
    public static class LayerData {
 
        private final SimpleStringProperty layerName;
        private final DoubleProperty opacity;
 
        private LayerData(String layerName, double opacity) {
            this.layerName = new SimpleStringProperty(layerName);
            this.opacity = new SimpleDoubleProperty(opacity);
        }
 
        public String getLayerName() {
            return layerName.get();
        }
 
        public void setLayerName(String layerName) {
            this.layerName.set(layerName);
        }
 
        public double getOpacity() {
            return opacity.get();
        }
 
        public void setOpacity(double opacity) {
            this.opacity.set(opacity);
        }
 

    }
	
	 class TextEditingCell extends TableCell<LayerOpacityTable.LayerData, String> {
 
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
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0, 
                    Boolean arg1, Boolean arg2) {
                        if (!arg2) {
                            commitEdit(textField.getText());
                        }
                }
            });
        }
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
	 }
	 
	class OpacityEditingCell extends TableCell<LayerOpacityTable.LayerData, Double> {
 
        private Slider slider;
 
        public OpacityEditingCell() {
			createSlider();
        }
 
        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                setText(null);
                setGraphic(slider);
            }
        }
 
        @Override
        public void cancelEdit() {
            super.cancelEdit();
 
            slider.setValue(getItem());
            setGraphic(null);
        }
 
        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
 
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
				slider.setValue(getItem());
				setText(null);
				setGraphic(slider);
            }
        }
 
        private void createSlider() {
			slider = new Slider();
			slider.setMin(0);
			slider.setMax(1.0);

 //           slider.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            slider.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0, 
                    Boolean arg1, Boolean arg2) {
                        if (!arg2) {
                            commitEdit(slider.getValue());
                        }
                }
            });
        }
 
        private Double getDouble() {
            return getItem() == null ? 1.0 : getItem();
        }
    }

}
