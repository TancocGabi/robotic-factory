package ro.tuiasi.ac.robotic_factory;

import java.util.ArrayList;
import java.util.List;

public class Factory {
	private List<Robot> robots = new ArrayList<>();

	public void addRobot(Robot robot) {
		robots.add(robot);
	}

	public List<Robot> getRobots() {
		return robots;
	}

}
