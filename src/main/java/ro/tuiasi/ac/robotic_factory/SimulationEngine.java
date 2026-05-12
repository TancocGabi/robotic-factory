package ro.tuiasi.ac.robotic_factory;

import java.util.List;

public class SimulationEngine {
    private Factory factory;
    private List<Task> tasks;
    private volatile boolean running = true;
    private KafkaProducerService kafkaService;

    public SimulationEngine(Factory factory, List<Task> tasks) {
        this.factory = factory;
        this.tasks = tasks;
        this.kafkaService = KafkaProducerService.getInstance();
    }

    public void stop() {
        this.running = false;
        System.out.println("!!! COMANDA DE OPRIRE PRIMITA !!!");
    }

    public void start() {
        List<Robot> allRobots = factory.getRobots();

        List<Robot> processingPool = allRobots.stream()
                .filter(r -> r.getType().equals("processing")).toList();

        List<Robot> transportPool = allRobots.stream()
                .filter(r -> r.getType().equals("finalizer")).toList();

        System.out.println("--- START SIMULARE: " + tasks.size() + " task-uri ---");

        for (Task task : tasks) {
            if (!running) break;

            Robot selectedWorker = null;

            while (selectedWorker == null && running) {
                for (Robot r : processingPool) {
                    if (r.tryAcquire()) {
                        selectedWorker = r;
                        break;
                    }
                }
                if (selectedWorker == null) {
                    try { Thread.sleep(100); } catch (Exception e) { return; }
                }
            }

            if (selectedWorker == null) continue;

            final Robot worker = selectedWorker;
            worker.setCurrentTaskName(task.getName());
            worker.updateVisuals();

            kafkaService.sendEvent(new RobotEvent(
                worker.getId(), "TASK_STARTED", task.getName(), worker.getType()
            ));

            new Thread(() -> {
                try {
                    if (!running) { worker.release(); return; }

                    Thread.sleep(task.getDuration());

                    kafkaService.sendEvent(new RobotEvent(
                        worker.getId(), "TASK_COMPLETED", task.getName(),
                        "Durata: " + task.getDuration() + "ms"
                    ));

                    Robot selectedTransport = null;
                    while (selectedTransport == null && running) {
                        for (Robot rt : transportPool) {
                            if (rt.tryAcquire()) {
                                selectedTransport = rt;
                                break;
                            }
                        }
                        if (selectedTransport == null) Thread.sleep(100);
                    }

                    if (selectedTransport != null) {
                        final Robot transport = selectedTransport;

                        kafkaService.sendEvent(new RobotEvent(
                            transport.getId(), "PRODUCT_MOVED", task.getName(),
                            "De la: " + worker.getId() + " in depozit"
                        ));

                        transport.setCurrentTaskName("De la " + worker.getId());
                        transport.updateVisuals();

                        Thread.sleep(1000);

                        transport.release();
                        transport.setCurrentTaskName("Inactiv");
                        transport.updateVisuals();

                        kafkaService.sendEvent(new RobotEvent(
                            transport.getId(), "TASK_FINALIZED", task.getName(),
                            "Task complet depozitat"
                        ));

                    } else {
                        System.out.println("[STOP] " + worker.getId() +
                            " a abandonat task-ul " + task.getName() + " din cauza opririi.");
                    }

                    worker.release();
                    worker.setCurrentTaskName("Inactiv");
                    worker.updateVisuals();

                } catch (InterruptedException e) {
                    worker.release();
                    System.err.println("Thread intrerupt pentru " + worker.getId());
                }
            }).start();
        }
    }
}