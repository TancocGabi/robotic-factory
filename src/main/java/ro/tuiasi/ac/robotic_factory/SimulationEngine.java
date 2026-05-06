package ro.tuiasi.ac.robotic_factory;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private Factory factory;
    private List<Task> tasks;
    private KafkaProducerService kafkaService;
    private final List<Thread> allThreads = new ArrayList<>();

    public SimulationEngine(Factory factory, List<Task> tasks) {
        this.factory = factory;
        this.tasks = tasks;
        this.kafkaService = KafkaProducerService.getInstance();
    }

    public void start() {
        List<Robot> robots = factory.getRobots();
        Robot r1 = robots.get(0);
        Robot r2 = robots.get(1);
        Robot r3 = robots.get(2);

        for (Task task : tasks) {
            Robot selected = null;

            while (selected == null) {
                if (r1.tryAcquire()) {
                    selected = r1;
                } else if (r2.tryAcquire()) {
                    selected = r2;
                } else {
                    try { Thread.sleep(100); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }

            Robot worker = selected;

            Thread workerThread = new Thread(() -> {
                kafkaService.sendEvent(new RobotEvent(
                    worker.getId(), "TASK_STARTED", task.getName(), worker.getType()
                ));

                try { Thread.sleep(task.getDuration()); }
                catch (InterruptedException e) { e.printStackTrace(); }

                kafkaService.sendEvent(new RobotEvent(
                    worker.getId(), "TASK_COMPLETED", task.getName(),
                    "Durata: " + task.getDuration() + "ms"
                ));

                worker.release();

                Thread moverThread = new Thread(() -> {
                    while (!r3.tryAcquire()) {
                        try { Thread.sleep(100); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }

                    kafkaService.sendEvent(new RobotEvent(
                        "R3", "PRODUCT_MOVED", task.getName(),
                        "De la: " + worker.getId() + " in depozit"
                    ));

                    try { Thread.sleep(500); }
                    catch (InterruptedException e) { e.printStackTrace(); }

                    r3.release();
                });

                // adaugam SI pornim moverThread o singura data
                synchronized (allThreads) { allThreads.add(moverThread); }
                moverThread.start();
            });

            // adaugam SI pornim workerThread o singura data
            synchronized (allThreads) { allThreads.add(workerThread); }
            workerThread.start();
        }
    }

    public void awaitCompletion() throws InterruptedException {
        boolean done = false;
        while (!done) {
            Thread.sleep(200);
            synchronized (allThreads) {
                done = allThreads.stream().noneMatch(Thread::isAlive);
            }
        }
    }
}