package ro.tuiasi.ac.robotic_factory;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactoryGUI_tests {

    @Test
    void testGenerateTasksCount() {

        // cream obiectul FactoryGUI
        FactoryGUI gui = new FactoryGUI();

        // definim tipurile permise
        List<String> tipuri = Arrays.asList("Sudura", "Vopsire");

        // generam 5 taskuri
        List<Task> tasks = gui.generaTaskuriAleatorii(5, tipuri);

        // verificam daca au fost generate exact 5 taskuri
        assertEquals(5, tasks.size());
    }

    @Test
    void testGenerateTasksTypes() {

        // cream obiectul FactoryGUI
        FactoryGUI gui = new FactoryGUI();

        // tipurile permise
        List<String> tipuri = Arrays.asList("Sudura", "Vopsire");

        // generam 20 taskuri
        List<Task> tasks = gui.generaTaskuriAleatorii(20, tipuri);

        // verificam fiecare task generat
        for (Task t : tasks) {

            // verificam daca taskul apartine listei permise
            assertTrue(tipuri.contains(t.getName()));
        }
    }

    @Test
    void testTaskDurationRange() {

        // cream obiectul FactoryGUI
        FactoryGUI gui = new FactoryGUI();

        // folosim un singur tip de task
        List<String> tipuri = Arrays.asList("Sudura");

        // generam taskuri
        List<Task> tasks = gui.generaTaskuriAleatorii(20, tipuri);

        // verificam durata fiecarui task
        for (Task t : tasks) {

            // verificam durata minima
            assertTrue(t.getDuration() >= 1000);

            // verificam durata maxima
            assertTrue(t.getDuration() <= 3999);
        }
    }
}