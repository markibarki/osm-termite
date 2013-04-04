package intransix.osm.termite.render.source.dialog;

import intransix.osm.termite.render.source.SourceLayer;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
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
public class SourceListTable extends TableView<SourceLayer> {
 
    private final ObservableList<SourceLayer> data =
        FXCollections.observableArrayList();
	
	public SourceListTable() {
		init();
	}
	
	public void setData(List<SourceLayer> sourceLayers) {
		data.addAll(sourceLayers);
	}
   
 
    public final void init() {

        this.setEditable(true);
		
		final TableColumn layerNameCol = new TableColumn("Layer");
		final TableColumn visibleCol = new TableColumn("Visible");
		
        Callback<TableColumn, TableCell> cellFactory =
             new Callback<TableColumn, TableCell>() {
                 public TableCell call(TableColumn p) {
					if(p == layerNameCol) {
						 return new TextEditingCell();
					}
					else if(p == visibleCol) {
						return new BooleanEditingCell();
					}
					else {
						return null;
					}
                 }
             };
 
        
        layerNameCol.setMinWidth(100);
        layerNameCol.setCellValueFactory(
                new PropertyValueFactory<SourceLayer, String>("name"));
		layerNameCol.setCellFactory(cellFactory);
		layerNameCol.setOnEditCommit(
			new EventHandler<CellEditEvent<SourceLayer, String>>() {
				@Override
				public void handle(CellEditEvent<SourceLayer, String> t) {
					((SourceLayer) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setName(t.getNewValue());
				}
			}
		);
 
        visibleCol.setMinWidth(100);
        visibleCol.setCellValueFactory(
                new PropertyValueFactory<SourceLayer, Boolean>("isActive"));
		visibleCol.setCellFactory(cellFactory);
		visibleCol.setOnEditCommit(
			new EventHandler<CellEditEvent<SourceLayer, Boolean>>() {
				@Override
				public void handle(CellEditEvent<SourceLayer, Boolean> t) {
					((SourceLayer) t.getTableView().getItems().get(
						t.getTablePosition().getRow())
						).setIsActive(t.getNewValue());
				}
			}
		);
 
        this.setItems(data);
        this.getColumns().addAll(layerNameCol, visibleCol);
    }
	
	 class TextEditingCell extends TableCell<SourceLayer, String> {
 
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
	 
	class BooleanEditingCell extends TableCell<SourceLayer, Boolean> {
		private CheckBox checkBox;
		public BooleanEditingCell() {
			checkBox = new CheckBox();
			checkBox.setDisable(true);
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean> () {
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if(isEditing())
						commitEdit(newValue == null ? false : newValue);
				}
			});
			this.setGraphic(checkBox);
			this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			this.setEditable(true);
        }
        @Override
        public void startEdit() {
            super.startEdit();
            if (isEmpty()) {
                return;
            }
            checkBox.setDisable(false);
            checkBox.requestFocus();
        }
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            checkBox.setDisable(true);
        }
        public void commitEdit(Boolean value) {
            super.commitEdit(value);
            checkBox.setDisable(true);
        }
        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (!isEmpty()) {
                checkBox.setSelected(item);
            }
        }
    }
} 
