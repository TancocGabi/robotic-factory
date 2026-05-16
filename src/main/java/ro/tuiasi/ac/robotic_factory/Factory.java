package ro.tuiasi.ac.robotic_factory;

import java.util.ArrayList;
import java.util.List;

/**
 * Reprezintă fabrica automatizată care centralizează și gestionează resursele sistemului.
 * Această clasă acționează ca un container principal pentru colecția de roboți alocați
 * proceselor de producție din fabrică.
 *  @author Echipa Robotic Factory
 */
public class Factory {
    
    /** Lista care stochează toți roboții înregistrați în cadrul fabricii. */
    private List<Robot> robots = new ArrayList<>();

    /**
     * Înregistrează și adaugă un nou robot în sistemul fabricii.
     *
     * @param robot obiectul de tip {@link Robot} care va fi adăugat în listă
     */
    public void addRobot(Robot robot) {
        robots.add(robot);
    }

    /**
     * Returnează lista completă a roboților existenți în fabrică.
     *
     * @return o listă de tip {@link List} ce conține toți roboții din sistem
     */
    public List<Robot> getRobots() {
        return robots;
    }
}