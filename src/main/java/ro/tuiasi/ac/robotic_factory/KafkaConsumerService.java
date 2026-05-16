package ro.tuiasi.ac.robotic_factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Serviciu responsabil pentru consumarea asincronă a mesajelor dintr-un cluster Apache Kafka.
 * Rulează pe un fir de execuție separat (thread) pentru a nu bloca interfața grafică sau
 * fluxul principal al aplicației. Mesajele primite sub formă de text (JSON) sunt deserializate 
 * în obiecte de tip {@link RobotEvent} și transmise mai departe prin intermediul unui ascultător.
 * @author Echipa Robotic Factory
 */
public class KafkaConsumerService {
    
    /** Consumatorul nativ de Apache Kafka utilizat pentru citirea mesajelor. */
    private KafkaConsumer<String, String> consumer;
    
    /** Mapper-ul Jackson utilizat pentru conversia formatului JSON în obiecte Java Java. */
    private ObjectMapper objectMapper;
    
    /** Interfața de callback prin care se notifică recepția unui eveniment nou. */
    private EventListener listener;
    
    /** Firul de execuție dedicat pe care rulează bucla continuă de citire (polling) din Kafka. */
    private Thread consumerThread;
    
    /** * Indicator care marchează dacă serviciul a efectuat primul poll și este gata de utilizare.
     * Este marcat ca {@code volatile} pentru a asigura vizibilitatea modificărilor între thread-uri.
     */
    private volatile boolean ready = false;

    /**
     * Interfață funcțională (Listener) utilizată pentru a intercepta și procesa 
     * evenimentele recepționate de consumatorul Kafka.
     */
    public interface EventListener {
        /**
         * Metodă apelată automat la recepționarea cu succes a unui eveniment valid de la un robot.
         *
         * @param event obiectul {@link RobotEvent} extras din mesajul Kafka
         */
        void onEvent(RobotEvent event);
    }

    /**
     * Construiește un serviciu de consum Kafka și configurează proprietățile de conexiune.
     * Consumatorul este abonat în mod automat la topicul {@code factory-events}.
     *
     * @param listener instanța care va procesa evenimentele primite
     */
    public KafkaConsumerService(EventListener listener) {
        this.listener = listener;
        this.objectMapper = new ObjectMapper();
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "factory-monitor-" + System.currentTimeMillis());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList("factory-events"));
    }

    /**
     * Inițializează și pornește firul de execuție secundar care ascultă mesajele din Kafka.
     * Bucla de interogare (poll loop) rulează continuu până la întreruperea firului de execuție.
     * La oprire, resursele consumatorului sunt eliberate în mod securizat.
     */
    public void startListening() {
        consumerThread = new Thread(() -> {
            // Primul poll efectuat pentru alocarea partițiilor și stabilirea conexiunii active
            consumer.poll(Duration.ofMillis(3000));
            ready = true;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, String> record : records) {
                        try {
                            RobotEvent event = objectMapper.readValue(record.value(), RobotEvent.class);
                            if (listener != null) {
                                listener.onEvent(event);
                            }
                        } catch (Exception e) {
                            System.err.println("Eroare parsare: " + e.getMessage());
                        }
                    }
                } catch (org.apache.kafka.common.errors.InterruptException e) {
                    // Excepție aruncată specific de Kafka când thread-ul este întrerupt în timpul unui poll
                    break;
                }
            }
            Thread.interrupted(); // Curăță flag-ul de întrerupere
            consumer.close(); // Eliberează conexiunea cu serverul Kafka
        });
        consumerThread.start();
    }

    /**
     * Blochează firul de execuție curent până când consumatorul Kafka finalizează 
     * configurarea inițială și devine pregătit pentru citirea mesajelor.
     *
     * @throws InterruptedException dacă firul de execuție curent este întrerupt în timp ce așteaptă
     */
    public void waitUntilReady() throws InterruptedException {
        while (!ready) {
            Thread.sleep(100);
        }
    }

    /**
     * Oprește în mod controlat serviciul de ascultare prin trimiterea unui semnal 
     * de întrerupere către firul de execuție dedicat.
     */
    public void stop() {
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }
}