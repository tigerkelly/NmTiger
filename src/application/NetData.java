package application;

public class NetData {

	private long tcpPackets = 0;
	private long udpPackets = 0;
	private long otherPackets = 0;
	private long multicastPackets = 0;
	private double put;
	private String type;
	
	public NetData() {
		
	}
	
	public NetData(long tcpPackets, long udpPackets, long otherPackets, long multicastPackets, double put, String type) {
		super();
		this.tcpPackets = tcpPackets;
		this.udpPackets = udpPackets;
		this.otherPackets = otherPackets;
		this.multicastPackets = multicastPackets;
		this.put = put;
		this.type = type;
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

	public double getPut() {
		return put;
	}

	public void setPut(double put) {
		this.put = put;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getMulticastPackets() {
		return multicastPackets;
	}

	public void setMulticastPackets(long multicastPackets) {
		this.multicastPackets = multicastPackets;
	}
}
