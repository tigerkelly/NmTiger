package application;

public class BpsValue {

	private double mbps;
	private long t;
	
	public BpsValue() {
		
	}
	
	public BpsValue(double mbps, long t) {
		this.mbps = mbps;
		this.t = t;
	}

	public double getMbps() {
		return mbps;
	}

	public void setMbps(double mbps) {
		this.mbps = mbps;
	}

	public long getT() {
		return t;
	}

	public void setFac(long t) {
		this.t = t;
	}

	@Override
	public String toString() {
		return "BpsValue [mbps=" + mbps + ", t=" + t + "]";
	}
}
