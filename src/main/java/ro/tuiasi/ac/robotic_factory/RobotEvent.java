package ro.tuiasi.ac.robotic_factory;

/**
 * Reprezintă un eveniment generat de un robot în cadrul fabricii.
 * Această clasă funcționează ca un obiect de transfer de date (DTO) utilizat pentru 
 * monitorizarea activității roboților, stocarea istoricului sau trimiterea de mesaje 
 * către sisteme externe (cum ar fi Apache Kafka).
 *  @author Echipa Robotic Factory
 */
public class RobotEvent {
    
    /** Identificatorul unic al robotului care a generat evenimentul. */
    private String robotId;
    
    /** Tipul evenimentului (ex: "START_TASK", "FINISH_TASK", "ERROR"). */
    private String eventType;
    
    /** Numele task-ului asociat cu acest eveniment. */
    private String taskName;
    
    /** Timestamp-ul la care s-a produs evenimentul, exprimat în milisecunde de la Epoch. */
    private long timestamp;
    
    /** Detalii suplimentare sau mesaje descriptive specifice evenimentului. */
    private String details;

    /**
     * Constructor implicit (fără argumente).
     * Este necesar pentru mecanismele de reflectare și librăriile de serializare/deserializare (ex: Jackson JSON).
     */
    public RobotEvent() {}

    /**
     * Construiește un nou obiect de tip RobotEvent cu detaliile specificate.
     * Câmpul {@code timestamp} este inițializat automat cu timpul curent al sistemului.
     *
     * @param robotId   identificatorul robotului sursă
     * @param eventType tipul de eveniment raportat
     * @param taskName  numele sarcinii de lucru aflate în execuție sau procesate
     * @param details   informații adiționale despre starea robotului sau a operațiunii
     */
    public RobotEvent(String robotId, String eventType, String taskName, String details) {
        this.robotId = robotId;
        this.eventType = eventType;
        this.taskName = taskName;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returnează identificatorul robotului.
     *
     * @return un șir de caractere ce reprezintă ID-ul robotului
     */
    public String getRobotId() { return robotId; }

    /**
     * Setează identificatorul robotului care a generat evenimentul.
     *
     * @param robotId noul identificator al robotului
     */
    public void setRobotId(String robotId) { this.robotId = robotId; }

    /**
     * Returnează tipul evenimentului.
     *
     * @return tipul evenimentului sub formă de șir de caractere
     */
    public String getEventType() { return eventType; }

    /**
     * Setează tipul evenimentului.
     *
     * @param eventType noul tip de eveniment (ex: "START_TASK")
     */
    public void setEventType(String eventType) { this.eventType = eventType; }

    /**
     * Returnează numele task-ului corelat cu evenimentul.
     *
     * @return denumirea task-ului
     */
    public String getTaskName() { return taskName; }

    /**
     * Setează numele task-ului corelat cu acest eveniment.
     *
     * @param taskName denumirea noului task asociat
     */
    public void setTaskName(String taskName) { this.taskName = taskName; }

    /**
     * Returnează momentul de timp la care s-a produs evenimentul.
     *
     * @return valoarea timestamp-ului în milisecunde
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Setează manual momentul de timp al producerii evenimentului.
     *
     * @param timestamp valoarea timpului în milisecunde
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Returnează detaliile suplimentare ale evenimentului.
     *
     * @return un șir de caractere ce conține observații sau mesaje adiționale
     */
    public String getDetails() { return details; }

    /**
     * Setează detaliile sau descrierea suplimentară a evenimentului.
     *
     * @param details mesajul descriptiv asociat
     */
    public void setDetails(String details) { this.details = details; }
}