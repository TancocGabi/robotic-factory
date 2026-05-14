package ro.tuiasi.ac.robotic_factory;

import java.util.List;

/**
 * Motor de simulare pentru fabrica robotizată.
 *
 * <p>Această clasă coordonează întreaga execuție a simulării, gestionând alocarea
 * dinamică a roboților către task-uri și comunicarea evenimentelor prin Kafka.
 * Funcționează pe principiul unui pool de roboți: task-urile sunt distribuite
 * roboților de tip "processing" disponibili, iar după finalizare, roboții de tip
 * "finalizer" transportă produsele în depozit.</p>
 *
 * <p>Fluxul general de execuție este:</p>
 * <ol>
 *   <li>Se selectează un robot de procesare disponibil din pool.</li>
 *   <li>Robotul execută task-ul în mod asincron (pe un thread separat).</li>
 *   <li>După finalizarea task-ului, un robot de transport preia produsul.</li>
 *   <li>Produsul este mutat în depozit, iar ambii roboți revin la starea "Inactiv".</li>
 * </ol>
 *
 * <p>Simularea poate fi oprită oricând prin apelul metodei {@link #stop()},
 * care setează flag-ul {@code running} la {@code false} și întrerupe
 * alocarea de noi task-uri.</p>
 *
 * <p><b>Exemplu de utilizare:</b></p>
 * <pre>{@code
 * Factory factory = new Factory(...);
 * List<Task> tasks = Arrays.asList(new Task("Sudura", 2000), new Task("Vopsire", 3000));
 * SimulationEngine engine = new SimulationEngine(factory, tasks);
 * engine.start();
 * // ...mai târziu, pentru oprire:
 * engine.stop();
 * }</pre>
 *
 * @author Echipa Robotic Factory
 * @version 1.0
 * @see Factory
 * @see Robot
 * @see Task
 * @see KafkaProducerService
 */
public class SimulationEngine {

    /**
     * Fabrica asociată simulării, care conține toți roboții disponibili.
     * Prin intermediul acestui obiect se obține lista completă de roboți
     * ce vor fi împărțiți în pool-uri pe baza tipului lor.
     */
    private Factory factory;

    /**
     * Lista de task-uri ce urmează a fi executate în cadrul simulării.
     * Task-urile sunt procesate în ordine, fiecare fiind alocat primului
     * robot de procesare disponibil.
     */
    private List<Task> tasks;

    /**
     * Flag care controlează starea de rulare a simulării.
     *
     * <p>Declarat {@code volatile} pentru a garanta vizibilitatea modificărilor
     * între thread-uri: atunci când metoda {@link #stop()} este apelată din
     * alt thread, toate thread-urile active vor vedea imediat noua valoare
     * {@code false}, fără a utiliza o valoare din cache-ul local al procesorului.</p>
     *
     * <p>Cât timp valoarea este {@code true}, simularea continuă să aloce
     * task-uri și să aștepte roboți disponibili.</p>
     */
    private volatile boolean running = true;

    /**
     * Serviciul Kafka utilizat pentru trimiterea evenimentelor din simulare.
     *
     * <p>Prin intermediul acestuia se publică evenimente de tip
     * {@link RobotEvent} pe topicurile Kafka configurate, permițând
     * monitorizarea în timp real a stării simulării din sisteme externe.</p>
     *
     * @see KafkaProducerService
     * @see RobotEvent
     */
    private KafkaProducerService kafkaService;

    /**
     * Construiește un nou motor de simulare cu fabrica și lista de task-uri specificate.
     *
     * <p>La inițializare, se obține instanța singleton a serviciului Kafka,
     * pregătind infrastructura de mesagerie înainte de pornirea simulării.</p>
     *
     * @param factory fabrica robotizată ce conține toți roboții disponibili;
     *                nu trebuie să fie {@code null}
     * @param tasks   lista ordonată de task-uri ce vor fi executate în simulare;
     *                nu trebuie să fie {@code null} sau goală
     */
    public SimulationEngine(Factory factory, List<Task> tasks) {
        this.factory = factory;
        this.tasks = tasks;
        this.kafkaService = KafkaProducerService.getInstance();
    }

    /**
     * Oprește simularea în mod controlat (graceful shutdown).
     *
     * <p>Setează flag-ul intern {@code running} la {@code false}, semnalizând
     * tuturor thread-urilor active că trebuie să se oprească cât mai curând posibil.
     * Această metodă nu blochează — nu așteaptă finalizarea thread-urilor deja
     * pornite, ci doar previne pornirea unora noi și alocarea de noi task-uri.</p>
     *
     * <p>Thread-urile deja în execuție vor verifica flag-ul {@code running}
     * la următoarea oportunitate (de obicei după finalizarea sleep-ului curent)
     * și vor elibera roboții alocați înainte de a se termina.</p>
     *
     * <p><b>Notă:</b> Metoda este sigură pentru apel din orice thread
     * (thread-safe), datorită declarării câmpului {@code running} ca
     * {@code volatile}.</p>
     */
    public void stop() {
        this.running = false;
        System.out.println("!!! COMANDA DE OPRIRE PRIMITA !!!");
    }

    /**
     * Pornește simularea și coordonează execuția tuturor task-urilor.
     *
     * <p>Metoda parcurge lista de task-uri și, pentru fiecare, realizează
     * următorii pași:</p>
     * <ol>
     *   <li><b>Selectarea robotului de procesare:</b> Se caută în pool-ul
     *       de roboți "processing" primul robot liber prin {@code tryAcquire()}.
     *       Dacă niciun robot nu este disponibil, se așteaptă 100ms și se
     *       încearcă din nou (busy-wait cu pauze).</li>
     *   <li><b>Atribuirea task-ului:</b> Robotul selectat primește task-ul,
     *       vizualele sale sunt actualizate, și se trimite un eveniment Kafka
     *       de tip {@code TASK_STARTED}.</li>
     *   <li><b>Execuția asincronă:</b> Se lansează un thread nou care simulează
     *       execuția task-ului prin {@code Thread.sleep(task.getDuration())}.</li>
     *   <li><b>Transport la depozit:</b> După finalizare, thread-ul caută un
     *       robot "finalizer" liber și simulează transportul produsului
     *       (1000ms), trimitând evenimentele Kafka corespunzătoare.</li>
     *   <li><b>Eliberarea resurselor:</b> Ambii roboți sunt eliberați și
     *       revin la starea "Inactiv".</li>
     * </ol>
     *
     * <p><b>Concurență:</b> Fiecare task este executat pe propriul thread,
     * astfel că mai multe task-uri pot rula simultan, limitat de numărul
     * de roboți disponibili în pool-uri. Metoda principală ({@code start})
     * rulează pe thread-ul apelant și acționează ca un dispatcher.</p>
     *
     * <p><b>Oprire anticipată:</b> Dacă {@link #stop()} este apelat în
     * timpul execuției, alocarea de noi task-uri se oprește imediat,
     * iar thread-urile aflate în execuție se opresc la prima verificare
     * a flag-ului {@code running}.</p>
     *
     * <p><b>Gestionarea întreruperilor:</b> Dacă un thread este întrerupt
     * extern (prin {@code Thread.interrupt()}), robotul alocat este eliberat
     * și se afișează un mesaj de eroare.</p>
     *
     * <p><b>Evenimente Kafka trimise în cadrul unui task:</b></p>
     * <ul>
     *   <li>{@code TASK_STARTED} — robotul de procesare a preluat task-ul</li>
     *   <li>{@code TASK_COMPLETED} — procesarea a fost finalizată</li>
     *   <li>{@code PRODUCT_MOVED} — robotul de transport mută produsul</li>
     *   <li>{@code TASK_FINALIZED} — produsul a ajuns în depozit</li>
     * </ul>
     */
    public void start() {
        List<Robot> allRobots = factory.getRobots();

        // Împărțirea roboților în două pool-uri distincte pe baza tipului lor
        List<Robot> processingPool = allRobots.stream()
                .filter(r -> r.getType().equals("processing")).toList();

        List<Robot> transportPool = allRobots.stream()
                .filter(r -> r.getType().equals("finalizer")).toList();

        System.out.println("--- START SIMULARE: " + tasks.size() + " task-uri ---");

        for (Task task : tasks) {
            if (!running) break;

            Robot selectedWorker = null;

            // Busy-wait cu pauze de 100ms până se găsește un robot de procesare liber
            while (selectedWorker == null && running) {
                for (Robot r : processingPool) {
                    if (r.tryAcquire()) {
                        selectedWorker = r;
                        break;
                    }
                }
                if (selectedWorker == null) {
                    try { Thread.sleep(100); } catch (Exception e) { return; }
                }
            }

            if (selectedWorker == null) continue;

            final Robot worker = selectedWorker;
            worker.setCurrentTaskName(task.getName());
            worker.updateVisuals();

            kafkaService.sendEvent(new RobotEvent(
                worker.getId(), "TASK_STARTED", task.getName(), worker.getType()
            ));

            // Execuție asincronă: fiecare task rulează pe propriul thread
            new Thread(() -> {
                try {
                    if (!running) { worker.release(); return; }

                    // Simularea duratei de procesare a task-ului
                    Thread.sleep(task.getDuration());

                    kafkaService.sendEvent(new RobotEvent(
                        worker.getId(), "TASK_COMPLETED", task.getName(),
                        "Durata: " + task.getDuration() + "ms"
                    ));

                    // Căutarea unui robot de transport disponibil
                    Robot selectedTransport = null;
                    while (selectedTransport == null && running) {
                        for (Robot rt : transportPool) {
                            if (rt.tryAcquire()) {
                                selectedTransport = rt;
                                break;
                            }
                        }
                        if (selectedTransport == null) Thread.sleep(100);
                    }

                    if (selectedTransport != null) {
                        final Robot transport = selectedTransport;

                        kafkaService.sendEvent(new RobotEvent(
                            transport.getId(), "PRODUCT_MOVED", task.getName(),
                            "De la: " + worker.getId() + " in depozit"
                        ));

                        transport.setCurrentTaskName("De la " + worker.getId());
                        transport.updateVisuals();

                        // Simularea timpului de transport al produsului (1 secundă)
                        Thread.sleep(1000);

                        transport.release();
                        transport.setCurrentTaskName("Inactiv");
                        transport.updateVisuals();

                        kafkaService.sendEvent(new RobotEvent(
                            transport.getId(), "TASK_FINALIZED", task.getName(),
                            "Task complet depozitat"
                        ));

                    } else {
                        // Simularea a fost oprită înainte ca un transport să fie găsit
                        System.out.println("[STOP] " + worker.getId() +
                            " a abandonat task-ul " + task.getName() + " din cauza opririi.");
                    }

                    worker.release();
                    worker.setCurrentTaskName("Inactiv");
                    worker.updateVisuals();

                } catch (InterruptedException e) {
                    // Thread-ul a fost întrerupt extern; eliberăm robotul pentru a evita deadlock-uri
                    worker.release();
                    System.err.println("Thread intrerupt pentru " + worker.getId());
                }
            }).start();
        }
    }
}