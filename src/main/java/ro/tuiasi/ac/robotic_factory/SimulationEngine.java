package ro.tuiasi.ac.robotic_factory;

import java.util.List;

public class SimulationEngine {
	private Factory factory;
	private List<Task> tasks;

	public SimulationEngine(Factory factory, List<Task> tasks) {
		this.factory = factory;
		this.tasks = tasks;
	}

	public void start() {
		List<Robot> robots = factory.getRobots();

		Robot r1 = robots.get(0);
		Robot r2 = robots.get(1);
		Robot r3 = robots.get(2);

		for (Task task : tasks) {

			Robot selected = null;

			// alegem R1 sau R2
			while (selected == null) {

				if (r1.tryAcquire()) {
					selected = r1;
				} else if (r2.tryAcquire()) {
					selected = r2;
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			Robot worker = selected;

			new Thread(() -> {

				System.out.println(worker.getId() + " proceseaza " + task.getName());

				try {
					Thread.sleep(task.getDuration());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println(worker.getId() + " a terminat " + task.getName());

				worker.release();

				// R3
				new Thread(() -> {

					// asteapta pana R3 poate fi rezervat
					while (!r3.tryAcquire()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					System.out.println("R3 a mutat piesa prelucrata de " + worker.getId() + " in cadrul task-ului de "
							+ task.getName() + " in depozit");

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					r3.release();

				}).start();

			}).start();
		}
	}
}