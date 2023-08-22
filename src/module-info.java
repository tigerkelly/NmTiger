module NmTiger {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires java.desktop;
	requires javafx.base;
	requires java.base;
	requires java.logging;
	requires javafx.swing;
	requires com.sun.jna;
	requires org.pcap4j.core;
	
	opens application to javafx.graphics, javafx.fxml, java.base;
}
