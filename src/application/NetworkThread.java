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
import org.pcap4j.packet.Packet;

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
	private String type = null;
	
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
			
			long bytesSeen = 0;
			long bitsSeen = 0;
			long tcpPackets = 0;
			long udpPackets = 0;
			long otherPackets = 0;
			long mcPackets = 0;
			byte[] src = new byte[4];
			byte[] dst = new byte[4];
			
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
								 type = "Mbps";
								 break;
							 case NmGlobal.BYTES_PER_SEC:
								 mbps = (double)(bytesSeen / MEGA_BYTES) / ((d1 - d2) / 1000);
								 type = "MBps";
								 break;
							 case NmGlobal.KILOBITS_PER_SEC:
								 mbps = (double)(bitsSeen / KILO_BITS) / ((d1 - d2) / 1000);
								 type = "Kbps";
								 break;
							 case NmGlobal.KILOBYTES_PER_SEC:
								 mbps = (double)(bytesSeen / KILO_BYTES) / ((d1 - d2) / 1000);
								 type = "KBps";
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

								 if (ng.series.getData().size() > 30) {
									 ng.series.getData().remove(0);
								 }
								 ng.series.getData().add(new XYChart.Data<>(simpleDateFormat.format(dt), mbps));
							});
							
							if (statsChange != null) {
								statsChange.fireChange(new StatsChangeEvent(StatsChanges.STATS, 
										new NetData(tcpPackets, udpPackets, otherPackets, mcPackets, mbps, type)));
							}
							
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
					
					bytesSeen += p.length();
					bitsSeen = (bytesSeen * 8);
					
					byte[] data = p.getRawData();
					
					int protocol = (data[23] & 0xff);
					
					System.arraycopy(data, 26, src, 0, 4);
//					String srcAddr = String.format("%d.%d.%d.%d", (dst[0] & 0xff), (dst[1] & 0xff), (dst[2] & 0xff), (dst[3] &0xff));
					
					System.arraycopy(data, 30, dst, 0, 4);
					int octel = (dst[0] & 0xff);
//					String dstAddr = String.format("%d.%d.%d.%d", (dst[0] & 0xff), (dst[1] & 0xff), (dst[2] & 0xff), (dst[3] &0xff));
					
//					System.out.println(srcAddr + ", " + dstAddr);
					
					switch (protocol) {
					case 0x11:
						udpPackets++;
						if (octel >= 224 && octel < 240)
							mcPackets++;
						break;
					case 0x06:
						tcpPackets++;
						break;
					default:
						otherPackets++;
						break;
					}
					
					long v = ng.ipNumberStats[protocol];
					ng.ipNumberStats[protocol] = ++v;
				}
			}
	        
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
