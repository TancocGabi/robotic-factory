package ro.tuiasi.ac.robotic_factory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Clasa principală a interfeței grafice pentru simularea fabricii de roboți.
 *
 * <p>Extinde {@link javafx.application.Application} și gestionează:
 * <ul>
 *   <li>fereastra de configurare a simulării;</li>
 *   <li>interfața principală de monitorizare a roboților;</li>
 *   <li>afișarea evenimentelor primite prin Kafka.</li>
 * </ul>
 *
 * <p>Fluxul de execuție:
 * <ol>
 *   <li>Se pornește consumatorul Kafka ({@link KafkaConsumerService}).</li>
 *   <li>Se afișează dialogul de configurare
 *   ({@link #showSetupDialog(Stage)}).</li>
 *   <li>După confirmare, se inițializează UI-ul principal
 *   ({@link #initMainUI(Stage, int, int, List)}).</li>
 *   <li>Se pornește simularea prin {@link SimulationEngine}.</li>
 * </ol>
 *
 * @author Echipa Robotic Factory
 * @see Factory
 * @see SimulationEngine
 * @see KafkaConsumerService
 */
public class FactoryGUI extends Application {

    /** Instanța fabricii care gestionează roboții și task-urile. */
    private Factory factory;

    /** Containerul vizual în care sunt afișați roboții. */
    private FlowPane robotContainer;

    /** Motorul de simulare curent, folosit și pentru oprirea simulării. */
    private SimulationEngine currentEngine;

    /** Buton pentru oprirea simulării în curs. */
    private Button btnReset;

    /** Buton pentru resetarea fabricii și reluarea configurării. */
    private Button btnStop;

    /** Zona de text în care sunt afișate evenimentele primite din Kafka. */
    private TextArea logArea;

    /** Serviciul de consum Kafka pentru monitorizarea evenimentelor
     * din fabrică. */
    private KafkaConsumerService kafkaMonitor;

    /**
     * Lista opțiunilor de tipuri de task-uri disponibile pentru configurare.
     * Fiecare element reprezintă un tip de operație industrială.
     */
    private final String[] optiuniTaskuri = {
        "Sudura", "Ambalare", "Etichetare", "Asamblare",
        "Vopsire", "Testare", "Lipire", "Finisare"
    };

    /**
     * Punctul de intrare al aplicației JavaFX.
     *
     * <p>Inițializează serviciul Kafka, așteaptă până când acesta este
     * pregătit, apoi deschide fereastra de configurare a simulării.
     *
     * @param inputStage fereastra principală a aplicației, furnizată de
     *                   JavaFX
     */
    @Override
    public void start(final Stage inputStage) {
        kafkaMonitor = new KafkaConsumerService(event -> {
            Platform.runLater(() -> {
                switch (event.getEventType()) {
                    case "TASK_STARTED":
                        log(event.getRobotId() + " a inceput "
                            + event.getTaskName());
                        break;
                    case "TASK_COMPLETED":
                        log(event.getRobotId() + " a terminat "
                            + event.getTaskName());
                        break;
                    case "PRODUCT_MOVED":
                        log(event.getRobotId() + " a mutat produsul din "
                            + event.getTaskName() + " in depozit");
                        break;
                    case "TASK_FINALIZED":
                        log("[FINALIZAT] Task-ul " + event.getTaskName()
                            + " a fost depozitat.");
                        break;
                    default:
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

        showSetupDialog(inputStage);
    }

    /**
     * Adaugă un mesaj în zona de log a interfeței grafice.
     *
     * <p>Dacă {@link #logArea} nu a fost încă inițializat, metoda nu face
     * nimic.
     *
     * @param inputMessage mesajul de afișat; se adaugă pe o linie nouă
     */
    private void log(final String inputMessage) {
        if (logArea != null) {
            logArea.appendText(inputMessage + "\n");
        }
    }

    /**
     * Afișează fereastra de configurare a simulării.
     *
     * <p>Permite utilizatorului să definească:
     * <ul>
     *   <li>numărul de roboți de procesare;</li>
     *   <li>numărul de roboți de transport;</li>
     *   <li>numărul total de task-uri de generat;</li>
     *   <li>tipurile de task-uri permise (selectate prin
     *   {@code CheckBox}).</li>
     * </ul>
     *
     * <p>La apăsarea butonului <em>Pornește Fabrica</em>, se validează
     * inputul, se generează lista de task-uri și se trece la
     * {@link #initMainUI(Stage, int, int, List)}.
     *
     * @param inputStage fereastra principală, transmisă mai departe pentru
     *                   inițializarea UI-ului
     */
    private void showSetupDialog(final Stage inputStage) {
        Stage configStage = new Stage();
        configStage.setTitle("Configurare Simulare");

        VBox layout = new VBox(SPACING_MEDIUM);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center-left;");

        Label lblRoboti = new Label("Număr roboți procesare:");
        TextField txtRoboti = new TextField("3");

        Label lblRobotiTransport = new Label("Număr roboți transport (RT):");
        TextField txtRobotiTransport = new TextField("1");

        Label lblNrTaskuri = new Label("Număr total de task-uri de generat:");
        TextField txtNrTaskuri = new TextField("10");

        Label lblSelectie = new Label("Selectați tipurile de task-uri "
            + "permise:");
        VBox checkBoxesContainer = new VBox(SPACING_SMALL);
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (String nume : optiuniTaskuri) {
            CheckBox cb = new CheckBox(nume);
            cb.setSelected(true);
            checkBoxes.add(cb);
            checkBoxesContainer.getChildren().add(cb);
        }

        Button btnStart = new Button("Pornește Fabrica");
        btnStart.setStyle("-fx-background-color: #4CAF50; "
            + "-fx-text-fill: white; -fx-font-weight: bold;");

        btnStart.setOnAction(e -> {
            try {
                int nRobotiProc = Integer.parseInt(txtRoboti.getText());
                int nRobotiTransp =
                    Integer.parseInt(txtRobotiTransport.getText());
                int nTaskuri = Integer.parseInt(txtNrTaskuri.getText());

                List<String> taskuriSelectate = new ArrayList<>();
                for (CheckBox cb : checkBoxes) {
                    if (cb.isSelected()) {
                        taskuriSelectate.add(cb.getText());
                    }
                }

                if (taskuriSelectate.isEmpty()) {
                    System.out.println("Selectați cel puțin un tip de "
                        + "task!");
                    return;
                }

                configStage.close();
                List<Task> listaFinala = generaTaskuriAleatorii(nTaskuri,
                    taskuriSelectate);
                initMainUI(inputStage, nRobotiProc, nRobotiTransp,
                    listaFinala);

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
        configStage.setScene(new Scene(layout,
            WINDOW_CONFIG_WIDTH, WINDOW_CONFIG_HEIGHT));
        configStage.show();
    }

    /**
     * Construiește reprezentarea vizuală a unui robot în interfață.
     *
     * <p>Fiecare robot este afișat ca un {@code VBox} ce conține:
     * <ul>
     *   <li>titlul cu ID-ul și tipul robotului;</li>
     *   <li>un dreptunghi colorat ({@link Rectangle}) care reflectă
     *   starea curentă;</li>
     *   <li>un text de status actualizat în timp real.</li>
     * </ul>
     *
     * <p>Referințele la {@link Rectangle} și la textul de status sunt
     * stocate direct pe obiectul {@link Robot} pentru actualizări
     * ulterioare din simulare.
     *
     * @param inputRobot robotul pentru care se creează reprezentarea UI
     * @return un {@code VBox} gata de adăugat în interfață
     */
    private VBox createRobotUI(final Robot inputRobot) {
        VBox container = new VBox(SPACING_SMALL);
        container.setStyle("-fx-alignment: center; -fx-border-color: #ddd; "
            + "-fx-padding: 10;");

        Text title = new Text(inputRobot.getId() + " ["
            + inputRobot.getType() + "]");
        Rectangle rect = new Rectangle(RECT_SIZE, RECT_SIZE);
        rect.setFill(Color.GREEN);
        rect.setArcWidth(ARC_SIZE);
        rect.setArcHeight(ARC_SIZE);
        inputRobot.setRepresentation(rect);

        Text status = new Text("Status: Inactiv");
        inputRobot.setStatusLabel(status);

        container.getChildren().addAll(title, rect, status);
        return container;
    }

    /**
     * Generează o listă de task-uri cu tipuri și durate aleatorii.
     *
     * <p>Durata fiecărui task este aleasă aleatoriu în intervalul
     * {@code [MIN_DURATION, MAX_DURATION)} ms. Tipul este ales aleatoriu
     * din lista {@code tipuriPermise}.
     *
     * @param inputCount        numărul de task-uri de generat
     * @param inputTipuriPermise lista tipurilor de task-uri din care se
     *                           poate alege
     * @return lista de {@link Task} generată
     */
    public List<Task> generaTaskuriAleatorii(final int inputCount,
            final List<String> inputTipuriPermise) {
        List<Task> generate = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < inputCount; i++) {
            String nume = inputTipuriPermise.get(
                random.nextInt(inputTipuriPermise.size()));
            int durata = MIN_DURATION + random.nextInt(MAX_DURATION
                - MIN_DURATION);
            generate.add(new Task(nume, durata));
        }
        return generate;
    }

    /**
     * Inițializează și afișează interfața principală de monitorizare a
     * fabricii.
     *
     * <p>Creează instanța {@link Factory}, adaugă roboții de procesare și
     * de transport, construiește layout-ul principal (zona roboților +
     * log Kafka) și pornește simularea.
     *
     * <p>Layout-ul este organizat astfel:
     * <ul>
     *   <li>un {@link HBox} cu butoanele de control (<em>Stop</em> și
     *   <em>Reset</em>);</li>
     *   <li>un {@link SplitPane} vertical cu zona roboților (65%) și
     *   zona de log (35%).</li>
     * </ul>
     *
     * @param inputStage fereastra principală pe care se setează noua scenă
     * @param inputNProc        numărul de roboți de procesare de creat
     * @param inputNTransp      numărul de roboți de transport
     *                          (finalizatori) de creat
     * @param inputTaskuri      lista de task-uri ce urmează să fie
     *                          executate în simulare
     */
    private void initMainUI(final Stage inputStage, final int inputNProc,
            final int inputNTransp, final List<Task> inputTaskuri) {
        factory = new Factory();

        for (int i = 1; i <= inputNProc; i++) {
            factory.addRobot(new Robot("R" + i, "processing"));
        }
        for (int i = 1; i <= inputNTransp; i++) {
            factory.addRobot(new Robot("RT" + i, "finalizer"));
        }

        btnStop = new Button("Stop Simulare");
        btnStop.setStyle("-fx-background-color: #ff9800; "
            + "-fx-text-fill: white;");

        btnReset = new Button("Reset Fabrica");
        btnReset.setDisable(true);
        btnReset.setStyle("-fx-background-color: #f44336; "
            + "-fx-text-fill: white;");

        btnStop.setOnAction(e -> {
            if (currentEngine != null) {
                currentEngine.stop();
                btnStop.setDisable(true);
                btnReset.setDisable(false);
                log("--- Simularea a fost oprita de utilizator ---");
            }
        });

        btnReset.setOnAction(e -> {
            if (logArea != null) {
                logArea.clear();
            }
            showSetupDialog(inputStage);
        });

        HBox controls = new HBox(SPACING_MEDIUM, btnStop, btnReset);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        robotContainer = new FlowPane(SPACING_LARGE, SPACING_LARGE);
        robotContainer.setStyle("-fx-padding: 20; -fx-alignment: center;");
        for (Robot r : factory.getRobots()) {
            robotContainer.getChildren().add(createRobotUI(r));
        }

        ScrollPane scrollRoboti = new ScrollPane(robotContainer);
        scrollRoboti.setFitToWidth(true);
        scrollRoboti.setPannable(true);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");

        Label lblLog = new Label("Evenimente Kafka:");
        lblLog.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 0 10;");

        VBox logBox = new VBox(lblLog, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(scrollRoboti, logBox);
        splitPane.setDividerPositions(DIVIDER_POSITION);

        VBox mainLayout = new VBox(controls, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout,
            WINDOW_MAIN_WIDTH, WINDOW_MAIN_HEIGHT);
        inputStage.setTitle("Monitorizare Fabrică");
        inputStage.setScene(scene);
        inputStage.show();

        startSimulation(inputTaskuri);
    }

    /**
     * Pornește motorul de simulare pe un fir de execuție separat
     * (daemon thread).
     *
     * <p>Instanța {@link SimulationEngine} creată este stocată în
     * {@link #currentEngine} pentru a putea fi oprită ulterior prin
     * butonul <em>Stop</em>. Firul este marcat ca daemon, astfel încât
     * nu va bloca închiderea aplicației.
     *
     * @param inputTaskuri lista de task-uri ce vor fi procesate de
     *                     simulare
     */
    private void startSimulation(final List<Task> inputTaskuri) {
        currentEngine = new SimulationEngine(factory, inputTaskuri);
        Thread simThread = new Thread(() -> currentEngine.start());
        simThread.setDaemon(true);
        simThread.start();
    }

    /**
     * Metoda principală de lansare a aplicației JavaFX.
     *
     * @param inputArgs argumentele din linia de comandă (neutilizate)
     */
    public static void main(final String[] inputArgs) {
        launch(inputArgs);
    }

    // Constante pentru magic numbers
    /** Spațierea mică între elementele UI. */
    private static final int SPACING_SMALL = 5;

    /** Spațierea medie între elementele UI. */
    private static final int SPACING_MEDIUM = 15;

    /** Spațierea mare între elementele UI. */
    private static final int SPACING_LARGE = 20;

    /** Dimensiunea laturii pătratului ce reprezintă robotul (în pixeli). */
    private static final int RECT_SIZE = 60;

    /** Raza de curbură a colțurilor dreptunghiului robotului (în pixeli). */
    private static final int ARC_SIZE = 10;

    /** Durata minimă de execuție a unui task (în milisecunde). */
    private static final int MIN_DURATION = 1000;

    /** Durata maximă de execuție a unui task (în milisecunde). */
    private static final int MAX_DURATION = 4000;

    /** Lățimea ferestrei de configurare (în pixeli). */
    private static final int WINDOW_CONFIG_WIDTH = 350;

    /** Înălțimea ferestrei de configurare (în pixeli). */
    private static final int WINDOW_CONFIG_HEIGHT = 500;

    /** Lățimea ferestrei principale de monitorizare (în pixeli). */
    private static final int WINDOW_MAIN_WIDTH = 900;

    /** Înălțimea ferestrei principale de monitorizare (în pixeli). */
    private static final int WINDOW_MAIN_HEIGHT = 600;

    /** Poziția separatorului vertical între zona roboților și zona de log. */
    private static final double DIVIDER_POSITION = 0.65;
}
