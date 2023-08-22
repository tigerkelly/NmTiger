package application;

public class NetData {

	private long tcpPackets = 0;
	private long udpPackets = 0;
	private long otherPackets = 0;
	
	public NetData() {
		
	}
	
	public NetData(long tcpPackets, long udpPackets, long otherPackets) {
		super();
		this.tcpPackets = tcpPackets;
		this.udpPackets = udpPackets;
		this.otherPackets = otherPackets;
	}

	public long getTcpPackets() {
		return tcpPackets;
	}

	public void setTcpPackets(long tcpPackets) {
		this.tcpPackets = tcpPackets;
	}

	public long getUdpPackets() {
		return udpPackets;
	}

	public void setUdpPackets(long udpPackets) {
		this.udpPackets = udpPackets;
	}

	public long getOtherPackets() {
		return otherPackets;
	}

	public void setOtherPackets(long otherPackets) {
		this.otherPackets = otherPackets;
	}
}
