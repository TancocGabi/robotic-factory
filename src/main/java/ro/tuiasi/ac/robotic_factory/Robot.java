package ro.tuiasi.ac.robotic_factory;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;
import javafx.scene.text.Text;

public class Robot {
	private String id;
	private String type = "processing";
	private boolean busy;
	
	//javaFx
	private Rectangle representation;
	private Text statusLabel;
	private String currentTaskName = "Inactiv";
	
	public void setRepresentation(Rectangle rect) {
	    this.representation = rect;
	}

	public void updateVisuals() {
	    if (representation != null && statusLabel != null) {
	        Platform.runLater(() -> {
	            representation.setFill(isBusy() ? Color.RED : Color.GREEN);
	            statusLabel.setText(isBusy() ? "Task: " + currentTaskName : "Status: Inactiv");
	        });
	    }
	}
	
	public void setStatusLabel(Text label) {
	    this.statusLabel = label;
	}
	
	public void setCurrentTaskName(String taskName) {
	    this.currentTaskName = taskName;
	}

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
		updateVisuals();
		return true;
	}

	public synchronized void release() {
		busy = false;
		updateVisuals();
	}
}