package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SnapshotController implements Initializable {

	private NmGlobal ng = NmGlobal.getInstance();
	
    @FXML
    private AnchorPane aPane;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private TextField tfNewName;

    @FXML
    private TextField tfOldName;

    @FXML
    void doBtnCancel(ActionEvent event) {
    	ng.snapshotName = null;
    	Stage stage = (Stage)aPane.getScene().getWindow();
    	stage.close();
    }

    @FXML
    void doBtnSave(ActionEvent event) {
    	String sn = tfNewName.getText();
    	if (sn.isBlank() == true)
    		return;
    	
    	ng.snapshotName = new File(sn);
    	ng.lastSnapshotName = new File(sn);
    	
    	Stage stage = (Stage)aPane.getScene().getWindow();
    	stage.close();
    }

    @FXML
    void doSelect(ActionEvent event) {
    	Stage stage = (Stage)aPane.getScene().getWindow();
    	
    	FileChooser fc = new FileChooser();
    	fc.setInitialDirectory(new File(System.getProperty("user.home")));
    	fc.getExtensionFilters().addAll(
		     new FileChooser.ExtensionFilter("PNG Files", "*.png"),
		     new FileChooser.ExtensionFilter("All Files", "*.*")
		);
    	File fd = fc.showOpenDialog(stage);
		if (fd != null) {
			tfNewName.setText(fd.getAbsolutePath());
		}
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		if (ng.lastSnapshotName != null)
			tfNewName.setText(ng.lastSnapshotName.getAbsolutePath());
	}
	
	public Stage getStage() {
		return (Stage)aPane.getScene().getWindow();
	}
	
	public void setOldName(String oldName) {
		tfOldName.setText(oldName);
	}

}
