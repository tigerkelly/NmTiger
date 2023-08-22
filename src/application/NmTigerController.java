package application;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class NmTigerController implements Initializable {
	
	private NmGlobal ng = NmGlobal.getInstance();
	private StatsChanges statsChanges = null;
	
	final static int CHART_WIDTH = 1000;
	final static int CHART_HEIGHT = 650;

    @FXML
    private AnchorPane aPane;

    @FXML
    private Button btnAbout;
    
    @FXML
    private Menu mInterface;
    
    @FXML
    private Label lblTcpPackets;
    
    @FXML
    private Label lblUdpPackets;
    
    @FXML
    private Label lblOtherPackets;
    
    @FXML
    private HBox hbChart;
    
    @FXML
    private ToggleGroup bitToogle;
    
    @FXML
    private RadioMenuItem mBitsPerSec;

    @FXML
    private RadioMenuItem mBytesPerSec;
    
    @FXML
    void doKilobitsPerSec(ActionEvent event) {
    	ng.series.getData().clear();
    	ng.bitsPerSec = NmGlobal.KILOBITS_PER_SEC;
    	NumberAxis n = (NumberAxis)ng.lineChart.getYAxis();
    	n.setLabel("Kilobits per second");
    }
    
    @FXML
    void doKilobytesPerSec(ActionEvent event) {
    	ng.series.getData().clear();
    	ng.bitsPerSec = NmGlobal.KILOBYTES_PER_SEC;
    	NumberAxis n = (NumberAxis)ng.lineChart.getYAxis();
    	n.setLabel("Kilobytes per second");
    }

    @FXML
    void doBytesPerSec(ActionEvent event) {
    	ng.series.getData().clear();
    	ng.bitsPerSec = NmGlobal.BYTES_PER_SEC;
    	NumberAxis n = (NumberAxis)ng.lineChart.getYAxis();
    	n.setLabel("Megabytes per second");
    }
    
    @FXML
    void doBitsPerSec(ActionEvent event) {
    	ng.series.getData().clear();
    	ng.bitsPerSec = NmGlobal.BITS_PER_SEC;
    	NumberAxis n = (NumberAxis)ng.lineChart.getYAxis();
    	n.setLabel("Megabits per second");
    }
    
    @FXML
    void doBtnAbout(ActionEvent event) {
    	ng.centerScene(aPane, "About.fxml", "About NmTiger", null);
    }
    
    @FXML
    void doFileQuit(ActionEvent event) {
    	Stage stage = (Stage)aPane.getScene().getWindow();
    	stage.close();
    }
    
    @FXML
    void doFileSnapshot(ActionEvent event) {
    	WritableImage wi = ng.lineChart.snapshot(new SnapshotParameters(), new WritableImage(CHART_WIDTH, CHART_HEIGHT));
    	File file = new File("CanvasImage.png");
    	try {
    	    ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file);
    	    
    	    FXMLLoader loader = ng.loadScene(aPane, "Snapshot.fxml", "Rename Snapshot", null);
    	    SnapshotController sc = (SnapshotController)loader.getController();
    	    sc.setOldName(file.getAbsolutePath());
    	    
    	    Stage stage = sc.getStage();
    	    
    	    stage.showAndWait();
    	    
    	    if (ng.snapshotName != null) {
    	    	ng.copyFile(file, ng.snapshotName);
    	    	file.delete();
    	    }
    	} catch (Exception s) {
    	}
    }
    
    @FXML
    void doHelpHelp(ActionEvent event) {
    	ng.centerScene(aPane, "Help.fxml", "NmTiger Help", null);
    }
    
    @FXML
    void doBtnStats(ActionEvent event) {
    	ng.centerScene(aPane, "OtherStats.fxml", "Other Stats", null);
    }
    
    @FXML
    void doBtnStop(ActionEvent event) {
    	if (ng.monThread != null)
    		ng.monThread.stopIt();
    	
    	ObservableList<MenuItem> items = mInterface.getItems();
    	
    	if (items != null) {
    		for (MenuItem m : items) {
    			Menu mm = (Menu)m;
    			ObservableList<MenuItem> mms = mm.getItems();
    			if (mms != null) {
    				for (MenuItem item : mms) {
    					RadioMenuItem rm = (RadioMenuItem)item;
    					rm.setSelected(false);
    				}
    			}
    		}
    	}
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		//defining the axes
        CategoryAxis xAxis = new CategoryAxis(); // we are gonna plot against time
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false); // axis animations are removed
        switch (ng.bitsPerSec) {
        case NmGlobal.BITS_PER_SEC:
        	yAxis.setLabel("Megabits per second");
			break;
		case NmGlobal.BYTES_PER_SEC:
			yAxis.setLabel("Megabytes per second");
			break;
		case NmGlobal.KILOBITS_PER_SEC:
			yAxis.setLabel("Kilobits per second");
			break;
		case NmGlobal.KILOBYTES_PER_SEC:
			yAxis.setLabel("Kilobytes per second");
			break;
        }
        yAxis.setAnimated(false); // axis animations are removed
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10.0);

        //creating the line chart with two axis created above
        ng.lineChart = new LineChart<>(xAxis, yAxis);
        ng.lineChart.setPrefWidth(CHART_WIDTH);
        ng.lineChart.setPrefHeight(CHART_HEIGHT);
        ng.lineChart.setTitle("Network");
        ng.lineChart.setAnimated(false); // disable animations
        
        ng.series.setName("Data Series");

        // add series to chart
        ng.lineChart.getData().add(ng.series);
        ng.lineChart.setStyle("CHART_COLOR_1: blue;");
        
        HBox.setHgrow(ng.lineChart, Priority.ALWAYS);
        
//        hbChart.setStyle("-fx-border-color: black;");
        
        hbChart.getChildren().add(ng.lineChart);
        
        try {
	        List<PcapNetworkInterface> devs = Pcaps.findAllDevs();
	        
	        ToggleGroup t = new ToggleGroup();
			
			if (devs != null) {
				int n = 1;
				for (PcapNetworkInterface d : devs) {
					Menu m = new Menu("Interface-" + n++);
					mInterface.getItems().add(m);
					
					List<PcapAddress> addrs = d.getAddresses();
					if (addrs != null) {
						for (PcapAddress a : addrs) {
							RadioMenuItem r = new RadioMenuItem(a.getAddress().getHostAddress());
							r.setToggleGroup(t);
							r.setUserData(d.getDescription());
							r.setOnAction((e) -> {
								if (ng.monThread != null) {
									ng.monThread.stopIt();
								}
								
								ng.series.getData().clear();
								ng.series.setName(r.getText());
								ng.lineChart.setTitle((String)r.getUserData());
								ng.monThread = new NetworkThread(r.getText(), statsChanges);
								Thread th = new Thread(ng.monThread);
								th.setDaemon(true);
								th.start();
							});
							m.getItems().add(r);
						}
					}
				}
			}
		} catch (PcapNativeException e) {
			e.printStackTrace();
		}
        
        statsChanges = new StatsChanges();
        
        statsChanges.addChangeListener(new StatsChangeListener() {

			@Override
			public void changeEventOccurred(StatsChangeEvent e) {
				final NetData data = e.data;
				if (data != null) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							lblTcpPackets.setText(data.getTcpPackets() + "");
							lblUdpPackets.setText(data.getUdpPackets() + "");
							lblOtherPackets.setText(data.getOtherPackets() + "");
						}
					});
				}
			}
        	
        });
	}

}
