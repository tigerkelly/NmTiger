package application;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Packet.IpV4Header;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.IpNumber;

import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class NetworkThread implements Runnable {
	
	private NmGlobal ng = NmGlobal.getInstance();
	private String ip = null;
	private boolean stopThread = false;
	private PcapHandle handle = null;
	private StatsChanges statsChange = null;
	private double mbps = 0.0;
	
	static final double MEGA_BITS = 1000000.0;
	static final double MEGA_BYTES = 1000000.0;
	static final double KILO_BITS = 1000.0;
	static final double KILO_BYTES = 1000.0;
	
	public NetworkThread(String ip, StatsChanges statsChange) {
		this.ip = ip;
		this.statsChange = statsChange;
	}
	
	@Override
	public void run() {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		
		try {
			List<PcapNetworkInterface> devs = Pcaps.findAllDevs();
			
			PcapNetworkInterface nif = null;
			for (PcapNetworkInterface d : devs) {
				for (PcapAddress pa : d.getAddresses()) {
					if (pa.getAddress().getHostAddress().equals(ip) == true)
						nif = d;
				}
			}
			
			if (nif == null)
				return;
			
//			System.out.println("nif " + nif);
			
			int snapLen = 65536;
			PromiscuousMode mode = PromiscuousMode.PROMISCUOUS;
			int timeout = 100;
			handle = nif.openLive(snapLen, mode, timeout);
			
			long d1 = 0;
			long d2 = 0;
			
//			long packetsSeen = 0;
			long bytesSeen = 0;
			long bitsSeen = 0;
			long tcpPackets = 0;
			long udpPackets = 0;
			long otherPackets = 0;
			
			if (statsChange != null)
				statsChange.fireChange(new StatsChangeEvent(StatsChanges.CLEAR_STATS, null));
			
			while (true) {
				if (stopThread == true)
					break;
				
				GregorianCalendar cal = new GregorianCalendar();
				
				d1 = cal.getTimeInMillis();
				
				if (d2 == 0) {
					d2 = d1;
				} else {
					final long dt = d2;
					if ((d1 - d2) >= 1000) {
						if (bitsSeen > 0) {
							switch (ng.bitsPerSec) {
							case NmGlobal.BITS_PER_SEC:
								mbps = (double)(bitsSeen / MEGA_BITS) / ((d1 - d2) / 1000);
								 break;
							 case NmGlobal.BYTES_PER_SEC:
								 mbps = (double)(bytesSeen / MEGA_BYTES) / ((d1 - d2) / 1000);
								 break;
							 case NmGlobal.KILOBITS_PER_SEC:
								 mbps = (double)(bitsSeen / KILO_BITS) / ((d1 - d2) / 1000);
								 break;
							 case NmGlobal.KILOBYTES_PER_SEC:
								 mbps = (double)(bytesSeen / KILO_BYTES) / ((d1 - d2) / 1000);
								 break;
							}
							
							double maxSeen = 0.0;
							for (Data<String, Number> d : ng.series.getData()) {
								if (d.getYValue().doubleValue() > maxSeen)
									maxSeen = d.getYValue().doubleValue();
							}
							
							final double mSeen = maxSeen;
							
							Platform.runLater(() -> {
								 NumberAxis n = (NumberAxis)ng.lineChart.getYAxis();
								
								 long newMax = (long)((mSeen / 30) + 1) * 30;
								 
								 if (newMax <= 50)
									 n.setTickUnit(5.0);
								 else if (newMax <= 100)
									 n.setTickUnit(10.0);
								 else if (newMax <= 300)
									 n.setTickUnit(20.0);
								 else if (newMax <= 500)
									 n.setTickUnit(50.0);
								 else if (newMax <= 700)
									 n.setTickUnit(100.0);
								 else
									 n.setTickUnit(200.0);
								 n.setUpperBound(newMax);

								 if (ng.series.getData().size() > 30)
									 ng.series.getData().remove(0);
								 ng.series.getData().add(new XYChart.Data<>(simpleDateFormat.format(dt), mbps));
							});
							
							if (statsChange != null)
								statsChange.fireChange(new StatsChangeEvent(StatsChanges.STATS, new NetData(tcpPackets, udpPackets, otherPackets)));
							
							bytesSeen = 0;
							bitsSeen = 0;
						} else {

							Platform.runLater(() -> {
								 if (ng.series.getData().size() > 20)
									 ng.series.getData().remove(0);
								 ng.series.getData().add(new XYChart.Data<>(simpleDateFormat.format(dt), 0.0));
							});
						}
						
						d2 = d1;
					}
				}
				
				Packet p = handle.getNextPacket();
				
				if (p != null) {
//					packetsSeen++;
					
					bytesSeen += p.length();
					bitsSeen = (bytesSeen * 8);
					
//					System.out.println("Got packet");
					
					if (p.contains(IpV4Packet.class) == true) {
						IpV4Header ipv4Hdr = p.get(IpV4Packet.class).getHeader();
						
						if (ipv4Hdr.getProtocol() == IpNumber.UDP)
							udpPackets++;
						else if (ipv4Hdr.getProtocol() == IpNumber.TCP)
							tcpPackets++;
						else {
							otherPackets++;
						}
						
						long v = ng.ipNumberStats[(int)ipv4Hdr.getProtocol().value()];
						ng.ipNumberStats[(int)ipv4Hdr.getProtocol().value()] = ++v;
						
//						Inet4Address src = ipv4Hdr.getSrcAddr();
//						Inet4Address dst = ipv4Hdr.getDstAddr();
						
//						System.out.println("packetSeen " + packetsSeen + ", bytesSeen " + bytesSeen + ", BitsSeen " + bitsSeen);
//						System.out.println("tcpPackets " + tcpPackets + ", udpPackets " + udpPackets);
//						System.out.println("src " + src + ", dst " + dst + "\n");
					}
				}
			}
			
//			PcapStat stats = handle.getStats();
//	        System.out.println("Packets received: " + stats.getNumPacketsReceived());
//	        System.out.println("Packets dropped: " + stats.getNumPacketsDropped());
//	        System.out.println("Packets dropped by interface: " + stats.getNumPacketsDroppedByIf());
//	        
//	        System.out.println("Packets captured: " +stats.getNumPacketsCaptured());
	        
			handle.close();
			
		} catch (PcapNativeException e) {
			e.printStackTrace();
		} catch (NotOpenException e) {
			e.printStackTrace();
		}
		
//		System.out.println("Leaving scan.");
	}
	
	public void stopIt() {
		stopThread = true;
	}

}
