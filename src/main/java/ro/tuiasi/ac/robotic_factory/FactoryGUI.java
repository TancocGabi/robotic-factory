package ro.tuiasi.ac.robotic_factory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FactoryGUI extends Application {

    private Factory factory;
    private FlowPane robotContainer;
    private SimulationEngine currentEngine;
    private Button btnReset;
    private Button btnStop;
    private TextArea logArea;
    private KafkaConsumerService kafkaMonitor;

    private final String[] OPTIUNI_TASKURI = {
        "Sudura", "Ambalare", "Etichetare", "Asamblare",
        "Vopsire", "Testare", "Lipire", "Finisare"
    };

    @Override
    public void start(Stage primaryStage) {
        kafkaMonitor = new KafkaConsumerService(event -> {
            Platform.runLater(() -> {
                switch (event.getEventType()) {
                    case "TASK_STARTED":
                        log(event.getRobotId() + " a inceput " + event.getTaskName());
                        break;
                    case "TASK_COMPLETED":
                        log(event.getRobotId() + " a terminat " + event.getTaskName());
                        break;
                    case "PRODUCT_MOVED":
                        log(event.getRobotId() + " a mutat produsul din " +
                            event.getTaskName() + " in depozit");
                        break;
                    case "TASK_FINALIZED":
                        log("[FINALIZAT] Task-ul " + event.getTaskName() + " a fost depozitat.");
                        break;
                }
            });
        });

        try {
            kafkaMonitor.startListening();
            kafkaMonitor.waitUntilReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        showSetupDialog(primaryStage);
    }

    private void log(String message) {
        if (logArea != null) {
            logArea.appendText(message + "\n");
        }
    }

    private void showSetupDialog(Stage primaryStage) {
        Stage configStage = new Stage();
        configStage.setTitle("Configurare Simulare");

        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center-left;");

        Label lblRoboti = new Label("Număr roboți procesare:");
        TextField txtRoboti = new TextField("3");

        Label lblRobotiTransport = new Label("Număr roboți transport (RT):");
        TextField txtRobotiTransport = new TextField("1");

        Label lblNrTaskuri = new Label("Număr total de task-uri de generat:");
        TextField txtNrTaskuri = new TextField("10");

        Label lblSelectie = new Label("Selectați tipurile de task-uri permise:");
        VBox checkBoxesContainer = new VBox(5);
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (String nume : OPTIUNI_TASKURI) {
            CheckBox cb = new CheckBox(nume);
            cb.setSelected(true);
            checkBoxes.add(cb);
            checkBoxesContainer.getChildren().add(cb);
        }

        Button btnStart = new Button("Pornește Fabrica");
        btnStart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        btnStart.setOnAction(e -> {
            try {
                int nRobotiProc = Integer.parseInt(txtRoboti.getText());
                int nRobotiTransp = Integer.parseInt(txtRobotiTransport.getText());
                int nTaskuri = Integer.parseInt(txtNrTaskuri.getText());

                List<String> taskuriSelectate = new ArrayList<>();
                for (CheckBox cb : checkBoxes) {
                    if (cb.isSelected()) taskuriSelectate.add(cb.getText());
                }

                if (taskuriSelectate.isEmpty()) {
                    System.out.println("Selectați cel puțin un tip de task!");
                    return;
                }

                configStage.close();
                List<Task> listaFinala = generaTaskuriAleatorii(nTaskuri, taskuriSelectate);
                initMainUI(primaryStage, nRobotiProc, nRobotiTransp, listaFinala);

            } catch (NumberFormatException ex) {
                System.out.println("Introduceți numere valide!");
            }
        });

        layout.getChildren().addAll(
            lblRoboti, txtRoboti,
            lblRobotiTransport, txtRobotiTransport,
            lblNrTaskuri, txtNrTaskuri,
            lblSelectie, checkBoxesContainer,
            btnStart
        );
        configStage.setScene(new Scene(layout, 350, 500));
        configStage.show();
    }

    private VBox createRobotUI(Robot r) {
        VBox container = new VBox(10);
        container.setStyle("-fx-alignment: center; -fx-border-color: #ddd; -fx-padding: 10;");

        Text title = new Text(r.getId() + " [" + r.getType() + "]");
        Rectangle rect = new Rectangle(60, 60);
        rect.setFill(Color.GREEN);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        r.setRepresentation(rect);

        Text status = new Text("Status: Inactiv");
        r.setStatusLabel(status);

        container.getChildren().addAll(title, rect, status);
        return container;
    }

    private List<Task> generaTaskuriAleatorii(int count, List<String> tipuriPermise) {
        List<Task> generate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            String nume = tipuriPermise.get(random.nextInt(tipuriPermise.size()));
            int durata = 1000 + random.nextInt(3000);
            generate.add(new Task(nume, durata));
        }
        return generate;
    }

    private void initMainUI(Stage primaryStage, int nProc, int nTransp, List<Task> taskuri) {
        factory = new Factory();

        for (int i = 1; i <= nProc; i++) {
            factory.addRobot(new Robot("R" + i, "processing"));
        }
        for (int i = 1; i <= nTransp; i++) {
            factory.addRobot(new Robot("RT" + i, "finalizer"));
        }

        btnStop = new Button("Stop Simulare");
        btnStop.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");

        btnReset = new Button("Reset Fabrica");
        btnReset.setDisable(true);
        btnReset.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        btnStop.setOnAction(e -> {
            if (currentEngine != null) {
                currentEngine.stop();
                btnStop.setDisable(true);
                btnReset.setDisable(false);
                log("--- Simularea a fost oprita de utilizator ---");
            }
        });

        btnReset.setOnAction(e -> {
            if (logArea != null) logArea.clear();
            showSetupDialog(primaryStage);
        });

        HBox controls = new HBox(15, btnStop, btnReset);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Roboti
        robotContainer = new FlowPane(20, 20);
        robotContainer.setStyle("-fx-padding: 20; -fx-alignment: center;");
        for (Robot r : factory.getRobots()) {
            robotContainer.getChildren().add(createRobotUI(r));
        }

        ScrollPane scrollRoboti = new ScrollPane(robotContainer);
        scrollRoboti.setFitToWidth(true);
        scrollRoboti.setPannable(true);

        // Log Kafka
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");

        Label lblLog = new Label("Evenimente Kafka:");
        lblLog.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 0 10;");

        VBox logBox = new VBox(lblLog, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS); // <-- log-ul creste cu fereastra

        // SplitPane vertical - permite tragerea in sus/jos
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(scrollRoboti, logBox);
        splitPane.setDividerPositions(0.65); // 65% roboti, 35% log

        // SplitPane se extinde cu fereastra
        VBox mainLayout = new VBox(controls, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 900, 600);
        primaryStage.setTitle("Monitorizare Fabrică");
        primaryStage.setScene(scene);
        primaryStage.show();

        startSimulation(taskuri);
    }

    private void startSimulation(List<Task> taskuri) {
        currentEngine = new SimulationEngine(factory, taskuri);
        Thread simThread = new Thread(() -> currentEngine.start());
        simThread.setDaemon(true);
        simThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}