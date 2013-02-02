package intransix.osm.termite.gui.property;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author sutter
 */
public class PropertyTable extends TableView<PropertyTable.PropertyPair> {
 
    private final ObservableList<PropertyPair> data =
        FXCollections.observableArrayList(
            new PropertyPair("Jacob", "Smith"),
            new PropertyPair("Isabella", "Johnson"),
            new PropertyPair("Ethan", "Williams"),
            new PropertyPair("Emma", "Jones"),
            new PropertyPair("Michael", "Brown")
        );
	
	public PropertyTable() {
		init();
	}
	
	public void addData(String key, String value) {
		data.add(new PropertyPair(key,value));
	}
   
 
    public void init() {

        this.setEditable(true);
		
        Callback<TableColumn, TableCell> cellFactory =
             new Callback<TableColumn, TableCell>() {
                 public TableCell call(TableColumn p) {
                    return new EditingCell();
                 }
             };
 
        TableColumn firstNameCol = new TableColumn("Key");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<PropertyPair, String>("key"));
		firstNameCol.setCellFactory(cellFactory);
		firstNameCol.setOnEditCommit(
			new EventHandler<CellEditEvent<PropertyPair, String>>() {
				@Override
				public void handle(CellEditEvent<PropertyPair, String> t) {
					((PropertyPair) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setKey(t.getNewValue());
				}
			}
		);
 
        TableColumn lastNameCol = new TableColumn("Value");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<PropertyPair, String>("value"));
		lastNameCol.setCellFactory(cellFactory);
		lastNameCol.setOnEditCommit(
			new EventHandler<CellEditEvent<PropertyPair, String>>() {
				@Override
				public void handle(CellEditEvent<PropertyPair, String> t) {
					((PropertyPair) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setValue(t.getNewValue());
				}
			}
		);
 
        this.setItems(data);
        this.getColumns().addAll(firstNameCol, lastNameCol);
    }
 
    public static class PropertyPair {
 
        private final SimpleStringProperty key;
        private final SimpleStringProperty value;
 
        private PropertyPair(String key, String value) {
            this.key = new SimpleStringProperty(key);
            this.value = new SimpleStringProperty(value);
        }
 
        public String getKey() {
            return key.get();
        }
 
        public void setKey(String propKey) {
            key.set(propKey);
        }
 
        public String getValue() {
            return value.get();
        }
 
        public void setValue(String propValue) {
            value.set(propValue);
        }
 

    }
	
	 class EditingCell extends TableCell<PropertyPair, String> {
 
        private TextField textField;
 
        public EditingCell() {
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
} 
