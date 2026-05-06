package ro.tuiasi.ac.robotic_factory;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        KafkaConsumerService monitor = new KafkaConsumerService(event -> {
            switch (event.getEventType()) {
                case "TASK_STARTED":
                    System.out.println(event.getRobotId() + " a inceput " + event.getTaskName());
                    break;
                case "TASK_COMPLETED":
                    System.out.println(event.getRobotId() + " a terminat " + event.getTaskName());
                    break;
                case "PRODUCT_MOVED":
                    System.out.println("R3 a mutat produsul din " + event.getTaskName() + " in depozit");
                    break;
            }
        });
        monitor.startListening();
        monitor.waitUntilReady();

        Factory factory = new Factory();
        factory.addRobot(new Robot("R1", "processing"));
        factory.addRobot(new Robot("R2", "processing"));
        factory.addRobot(new Robot("R3", "finalizer"));

        List<Task> tasks = Arrays.asList(
            new Task("Asamblare", 2000),
            new Task("Sudura", 3000),
            new Task("Vopsire", 1500),
            new Task("Ambalare", 1000),
            new Task("Inspectie", 2500)
        );

        SimulationEngine engine = new SimulationEngine(factory, tasks);
        engine.start();

        engine.awaitCompletion();

        monitor.stop();
        KafkaProducerService.getInstance().close();
    }
}