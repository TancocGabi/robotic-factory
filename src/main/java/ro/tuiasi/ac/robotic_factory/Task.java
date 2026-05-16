package ro.tuiasi.ac.robotic_factory;

/**
 * Reprezintă un task (o sarcină de lucru) care urmează să fie executat de un robot în fabrică.
 * Această clasă încapsulează informațiile de bază ale unui task, precum numele său
 * și durata necesară pentru finalizarea execuției.
 *  @author Echipa Robotic Factory
 */
public class Task {
    
    /** Numele sau descrierea scurtă a task-ului (ex: "Asamblare", "Vopsire"). */
    private String name;
    
    /** Durata de execuție a task-ului, exprimată de regulă în milisecunde sau secunde. */
    private int duration;

    /**
     * Construiește un nou obiect de tip Task cu numele și durata specificate.
     *
     * @param name     numele sau identificatorul simbolic al task-ului
     * @param duration timpul necesar pentru procesarea acestui task
     */
    public Task(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    /**
     * Returnează numele task-ului.
     *
     * @return un șir de caractere ce reprezintă denumirea task-ului
     */
    public String getName() {
        return name;
    }

    /**
     * Returnează durata de execuție a task-ului.
     *
     * @return un număr întreg ce reprezintă timpul alocat task-ului
     */
    public int getDuration() {
        return duration;
    }
}