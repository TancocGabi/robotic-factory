package ro.tuiasi.ac.robotic_factory;

public class Task {
	private String name;
	private int duration;

	public Task(String name, int duration) {
		this.name = name;
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public int getDuration() {
		return duration;
	}
}
