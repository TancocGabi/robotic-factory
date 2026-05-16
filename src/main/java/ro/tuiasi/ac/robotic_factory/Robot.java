package ro.tuiasi.ac.robotic_factory;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;
import javafx.scene.text.Text;

/**
 * Reprezintă un robot din cadrul fabricii automatizate.
 * Clasa gestionează starea de disponibilitate a robotului, tipul acestuia,
 * precum și elementele vizuale asociate în interfața grafică JavaFX.
 * * Accesele concurente la starea robotului sunt sincronizate pentru a preveni
 * alocarea simultană a aceluiași robot către task-uri diferite.
 *  @author Echipa Robotic Factory
 */
public class Robot {
    
    /** Identificatorul unic al robotului. */
    private String id;
    
    /** Tipul robotului (ex: "processing", "assembly" etc.). Valoarea implicită este "processing". */
    private String type = "processing";
    
    /** Indicator boolean care arată dacă robotul este ocupat cu un task în acest moment. */
    private boolean busy;
    
    /** Elementul grafic JavaFX (dreptunghi) care reprezintă robotul pe ecran. */
    private Rectangle representation;
    
    /** Elementul text JavaFX utilizat pentru afișarea stării actuale a robotului în UI. */
    private Text statusLabel;
    
    /** Numele task-ului curent pe care îl execută robotul. Valoarea implicită este "Inactiv". */
    private String currentTaskName = "Inactiv";
    
    /**
     * Construiește un nou obiect de tip Robot cu un identificator și un tip specificat.
     * Starea inițială a robotului este setată ca fiind disponibilă (busy = false).
     *
     * @param id   identificatorul unic al robotului
     * @param type tipul de activitate pe care o poate procesa robotul
     */
    public Robot(String id, String type) {
        this.id = id;
        this.type = type;
        this.busy = false;
    }

    /**
     * Setează reprezentarea geometrică a robotului pentru interfața grafică.
     *
     * @param rect obiectul {@link Rectangle} asociat robotului în UI
     */
    public void setRepresentation(Rectangle rect) {
        this.representation = rect;
    }

    /**
     * Actualizează elementele vizuale ale robotului pe firul de execuție JavaFX Application Thread.
     * Dacă robotul este ocupat, culoarea devine roșie și se afișează numele task-ului curent.
     * Dacă robotul este liber, culoarea devine verde și statusul se schimbă în "Inactiv".
     */
    public void updateVisuals() {
        if (representation != null && statusLabel != null) {
            Platform.runLater(() -> {
                representation.setFill(isBusy() ? Color.RED : Color.GREEN);
                statusLabel.setText(isBusy() ? "Task: " + currentTaskName : "Status: Inactiv");
            });
        }
    }
    
    /**
     * Setează eticheta de text destinată afișării statusului robotului în interfața grafică.
     *
     * @param label obiectul {@link Text} corespunzător din UI
     */
    public void setStatusLabel(Text label) {
        this.statusLabel = label;
    }
    
    /**
     * Setează numele task-ului pe care robotul îl execută în mod curent.
     *
     * @param taskName numele noului task atribuit
     */
    public void setCurrentTaskName(String taskName) {
        this.currentTaskName = taskName;
    }

    /**
     * Returnează identificatorul unic al robotului.
     *
     * @return un șir de caractere ce reprezintă ID-ul robotului
     */
    public String getId() {
        return id;
    }

    /**
     * Returnează tipul robotului.
     *
     * @return un șir de caractere ce reprezintă tipul robotului
     */
    public String getType() {
        return type;
    }

    /**
     * Verifică dacă robotul este ocupat în acest moment cu un task.
     *
     * @return {@code true} dacă robotul este ocupat, altfel {@code false}
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * Setează direct starea de ocupare a robotului.
     *
     * @param val {@code true} pentru a-l marca ocupat, {@code false} pentru a-l elibera
     */
    public void setBusy(boolean val) {
        this.busy = val;
    }

    /**
     * Încearcă să ocupe robotul în mod sincronizat.
     * Dacă robotul este deja ocupat, operațiunea eșuează. În caz contrar, robotul este 
     * marcat ca ocupat și reprezentarea sa vizuală este actualizată automat.
     *
     * @return {@code true} dacă robotul a fost achiziționat cu succes, 
     * sau {@code false} dacă era deja ocupat
     */
    public synchronized boolean tryAcquire() {
        if (busy)
            return false;
        busy = true;
        updateVisuals();
        return true;
    }

    /**
     * Eliberează robotul în mod sincronizat, marcându-l ca fiind disponibil (inactiv)
     * și actualizează elementele grafice din interfață.
     */
    public synchronized void release() {
        busy = false;
        updateVisuals();
    }
}