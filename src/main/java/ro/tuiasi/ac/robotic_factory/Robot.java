package ro.tuiasi.ac.robotic_factory;

public class Robot {
	private String id;
	private String type = "processing";
	private boolean busy;

	public Robot(String id, String type) {
		this.id = id;
		this.type = type;
		this.busy = false;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean val) {
		this.busy = val;
	}

	public synchronized boolean tryAcquire() {
		if (busy)
			return false;
		busy = true;
		return true;
	}

	public synchronized void release() {
		busy = false;
	}
}