package application;

import java.net.URL;
import java.util.ResourceBundle;

import org.pcap4j.packet.namednumber.IpNumber;

//import org.pcap4j.packet.namednumber.IpNumber;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class OtherStatsController implements Initializable {

	private NmGlobal ng = NmGlobal.getInstance();
	
    @FXML
    private AnchorPane aPane;

    @FXML
    private ListView<String> listView;
    
    @FXML
    void doClose(ActionEvent event) {
    	Stage stage = (Stage)aPane.getScene().getWindow();
    	stage.close();
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		ObservableList<String> stats = FXCollections.observableArrayList();
		
		for (int i = 0; i < 256; i++) {
			if (IpNumber.getInstance((byte)i).name().equalsIgnoreCase("unknown") == false)
				stats.add(String.format("%-25.25s %d", IpNumber.getInstance((byte)i).name(), ng.ipNumberStats[i]));
		}
		
		listView.setItems(stats);
	}

}