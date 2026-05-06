package ro.tuiasi.ac.robotic_factory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
	
	private final String[] OPTIUNI_TASKURI = {
		    "Sudura", "Ambalare", "Etichetare", "Asamblare", 
		    "Vopsire", "Testare", "Lipire", "Finisare"
		};

	@Override
	public void start(Stage primaryStage) {
	    showSetupDialog(primaryStage);
	}

	private void showSetupDialog(Stage primaryStage) {
	    Stage configStage = new Stage();
	    configStage.setTitle("Configurare Simulare");

	    VBox layout = new VBox(15);
	    layout.setStyle("-fx-padding: 20; -fx-alignment: center-left;");

	    // Input pentru Roboți
	    Label lblRoboti = new Label("Număr roboți procesare:");
	    TextField txtRoboti = new TextField("3");
	    
	    Label lblRobotiTransport = new Label("Număr roboți transport (RT):");
	    TextField txtRobotiTransport = new TextField("1"); // Default 1

	    // Input pentru număr total de Task-uri
	    Label lblNrTaskuri = new Label("Număr total de task-uri de generat:");
	    TextField txtNrTaskuri = new TextField("10");

	    // Selecție tipuri de task-uri (CheckBoxes)
	    Label lblSelectie = new Label("Selectați tipurile de task-uri permise:");
	    VBox checkBoxesContainer = new VBox(5);
	    List<CheckBox> checkBoxes = new ArrayList<>();

	    for (String nume : OPTIUNI_TASKURI) {
	        CheckBox cb = new CheckBox(nume);
	        cb.setSelected(true); // bifate implicit
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
	            
	            // Filtrăm doar ce a bifat utilizatorul
	            List<String> taskuriSelectate = new ArrayList<>();
	            for (CheckBox cb : checkBoxes) {
	                if (cb.isSelected()) taskuriSelectate.add(cb.getText());
	            }

	            if (taskuriSelectate.isEmpty()) {
	                System.out.println("Selectați cel puțin un tip de task!");
	                return;
	            }

	            configStage.close();
	            // Generăm lista finală de task-uri random din cele selectate
	            List<Task> listaFinala = generaTaskuriAleatorii(nTaskuri, taskuriSelectate);
	            
	            // Pornim interfața principală
	            initMainUI(primaryStage, nRobotiProc, nRobotiTransp, listaFinala);
	            
	        } catch (NumberFormatException ex) {
	            System.out.println("Introduceți numere valide!");
	        }
	    });

	    layout.getChildren().addAll(lblRoboti, txtRoboti, lblRobotiTransport, txtRobotiTransport  , lblNrTaskuri, txtNrTaskuri, lblSelectie, checkBoxesContainer, btnStart);
	    configStage.setScene(new Scene(layout, 350, 500));
	    configStage.show();
	}
	
	// Metodă pentru a crea UI-ul fiecărui robot
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
	        // Alege un nume la întâmplare din cele bifate
	        String nume = tipuriPermise.get(random.nextInt(tipuriPermise.size()));
	        // Durata între 1000ms și 4000ms
	        int durata = 1000 + random.nextInt(3000);
	        generate.add(new Task(nume, durata));
	    }
	    return generate;
	}

	private void initMainUI(Stage primaryStage, int nProc, int nTransp, List<Task> taskuri) {
	    factory = new Factory();
	    
	    // Generăm roboții
	    for (int i = 1; i <= nProc; i++) {
	        factory.addRobot(new Robot("R" + i, "processing"));
	    }
	    
	    // Adăugăm roboții de transport
	    for (int i = 1; i <= nTransp; i++) {
	        factory.addRobot(new Robot("RT" + i, "finalizer"));
	    }

	    btnStop = new Button("Stop Simulare");
	    btnStop.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
	    
	    btnReset = new Button("Reset Fabrica");
	    btnReset.setDisable(true); // Dezactivat la început
	    btnReset.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

	    btnStop.setOnAction(e -> {
	        if (currentEngine != null) {
	            currentEngine.stop();
	            btnStop.setDisable(true);
	            btnReset.setDisable(false); // Activăm Reset doar după Stop
	            System.out.println("Simularea a fost oprită de utilizator.");
	        }
	    });

	    btnReset.setOnAction(e -> {
	        showSetupDialog(primaryStage); // Revine la ecranul de configurare
	    });

	    HBox controls = new HBox(15, btnStop, btnReset);
	    controls.setStyle("-fx-padding: 10; -fx-alignment: center;");
	    // 2. Containerul pentru roboți (cu ScrollPane)
	    robotContainer = new FlowPane(20, 20);
	    robotContainer.setStyle("-fx-padding: 20; -fx-alignment: center;");
	    
	    for (Robot r : factory.getRobots()) {
	        robotContainer.getChildren().add(createRobotUI(r));
	    }

	    ScrollPane scrollPane = new ScrollPane(robotContainer);
	    scrollPane.setFitToWidth(true); // Se extinde pe lățime
	    scrollPane.setPannable(true);   // Permite scroll cu mouse drag

	    // 3. Layout Final
	    VBox mainLayout = new VBox(controls, scrollPane);
	    
	    Scene scene = new Scene(mainLayout, 900, 500);
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