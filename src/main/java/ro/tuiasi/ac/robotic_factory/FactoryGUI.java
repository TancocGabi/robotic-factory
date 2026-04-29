package ro.tuiasi.ac.robotic_factory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Arrays;
import java.util.List;

public class FactoryGUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        Factory factory = new Factory();
        factory.addRobot(new Robot("R1", "processing"));
        factory.addRobot(new Robot("R2", "processing"));
        factory.addRobot(new Robot("R3", "finalizer"));

        HBox root = new HBox(20); // Spațiere de 20px între elemente
        root.setStyle("-fx-padding: 50; -fx-alignment: center;");

        for (Robot r : factory.getRobots()) {
            VBox robotView = new VBox(10);
            robotView.setStyle("-fx-alignment: center;");

            Text title = new Text(r.getId());
            title.setStyle("-fx-font-weight: bold;");

            Rectangle rect = new Rectangle(80, 80);
            rect.setFill(Color.GREEN);
            rect.setArcWidth(15);
            rect.setArcHeight(15);
            r.setRepresentation(rect);

            // Adăugăm label-ul de status
            Text statusLabel = new Text("Status: Inactiv");
            r.setStatusLabel(statusLabel); // Îl dăm robotului

            robotView.getChildren().addAll(title, rect, statusLabel);
            root.getChildren().add(robotView);
        }

        Scene scene = new Scene(root, 500, 300);
        primaryStage.setTitle("Simulare Fabrică Roboți");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Pornim simularea pe un fir de execuție separat ca să nu blocheze interfața
        List<Task> tasks = Arrays.asList(
        		new Task("Asamblare", 2000),
				new Task("Sudura", 3000),
				new Task("Vopsire", 1500),
				new Task("Ambalare", 1000),
				new Task("Inspectie", 2500)
        );

        new Thread(() -> {
            SimulationEngine engine = new SimulationEngine(factory, tasks);
            engine.start();
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}