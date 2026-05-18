package ro.tuiasi.ac.robotic_factory;

/**
 * Reprezintă un eveniment generat de un robot în cadrul fabricii.
 * Această clasă funcționează ca un obiect de transfer de date (DTO) utilizat
 * pentru monitorizarea activității roboților, stocarea istoricului sau
 * trimiterea de mesaje către sisteme externe (cum ar fi Apache Kafka).
 * @author Echipa Robotic Factory
 */
public class RobotEvent {

    /** Identificatorul unic al robotului care a generat evenimentul. */
    private String robotId;

    /** Tipul evenimentului (ex: "START_TASK", "FINISH_TASK", "ERROR"). */
    private String eventType;

    /** Numele task-ului asociat cu acest eveniment. */
    private String taskName;

    /** Timestamp-ul la care s-a produs evenimentul, exprimat în milisecunde
     * de la Epoch. */
    private long timestamp;

    /** Detalii suplimentare sau mesaje descriptive specifice evenimentului. */
    private String details;

    /**
     * Constructor implicit (fără argumente).
     * Este necesar pentru mecanismele de reflectare și librăriile de
     * serializare/deserializare (ex: Jackson JSON).
     */
    public RobotEvent() {
    }

    /**
     * Construiește un nou obiect de tip RobotEvent cu detaliile specificate.
     * Câmpul {@code timestamp} este inițializat automat cu timpul curent al
     * sistemului.
     *
     * @param inputRobotId   identificatorul robotului sursă
     * @param inputEventType tipul de eveniment raportat
     * @param inputTaskName  numele sarcinii de lucru aflate în execuție sau
     *                       procesate
     * @param inputDetails   informații adiționale despre starea robotului
     *                       sau a operațiunii
     */
    public RobotEvent(final String inputRobotId, final String inputEventType,
            final String inputTaskName, final String inputDetails) {
        this.robotId = inputRobotId;
        this.eventType = inputEventType;
        this.taskName = inputTaskName;
        this.details = inputDetails;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returnează identificatorul robotului.
     *
     * @return un șir de caractere ce reprezintă ID-ul robotului
     */
    public String getRobotId() {
        return robotId;
    }

    /**
     * Setează identificatorul robotului care a generat evenimentul.
     *
     * @param inputRobotId noul identificator al robotului
     */
    public void setRobotId(final String inputRobotId) {
        this.robotId = inputRobotId;
    }

    /**
     * Returnează tipul evenimentului.
     *
     * @return tipul evenimentului sub formă de șir de caractere
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Setează tipul evenimentului.
     *
     * @param inputEventType noul tip de eveniment (ex: "START_TASK")
     */
    public void setEventType(final String inputEventType) {
        this.eventType = inputEventType;
    }

    /**
     * Returnează numele task-ului corelat cu evenimentul.
     *
     * @return denumirea task-ului
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Setează numele task-ului corelat cu acest eveniment.
     *
     * @param inputTaskName denumirea noului task asociat
     */
    public void setTaskName(final String inputTaskName) {
        this.taskName = inputTaskName;
    }

    /**
     * Returnează momentul de timp la care s-a produs evenimentul.
     *
     * @return valoarea timestamp-ului în milisecunde
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Setează manual momentul de timp al producerii evenimentului.
     *
     * @param inputTimestamp valoarea timpului în milisecunde
     */
    public void setTimestamp(final long inputTimestamp) {
        this.timestamp = inputTimestamp;
    }

    /**
     * Returnează detaliile suplimentare ale evenimentului.
     *
     * @return un șir de caractere ce conține observații sau mesaje adiționale
     */
    public String getDetails() {
        return details;
    }

    /**
     * Setează detaliile sau descrierea suplimentară a evenimentului.
     *
     * @param inputDetails mesajul descriptiv asociat
     */
    public void setDetails(final String inputDetails) {
        this.details = inputDetails;
    }
}
