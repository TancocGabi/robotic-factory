package ro.tuiasi.ac.robotic_factory;

import java.util.Arrays;
import java.util.List;

public class Main {
	public static void main(String[] args) {

		Factory factory = new Factory();

		// 3 roboți: R1, R2 procesare, R3 finalizare
		factory.addRobot(new Robot("R1", "processing"));
		factory.addRobot(new Robot("R2", "processing"));
		factory.addRobot(new Robot("R3", "finalizer"));

		// task-uri simulare
		List<Task> tasks = Arrays.asList(
				new Task("Asamblare", 2000),
				new Task("Sudura", 3000),
				new Task("Vopsire", 1500),
				new Task("Ambalare", 1000),
				new Task("Inspectie", 2500)
		);

		// pornim engine-ul
		SimulationEngine engine = new SimulationEngine(factory, tasks);
		engine.start();
	}
}