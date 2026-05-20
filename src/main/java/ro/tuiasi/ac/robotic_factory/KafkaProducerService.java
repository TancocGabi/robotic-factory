package ro.tuiasi.ac.robotic_factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

/**
 * Serviciu responsabil pentru trimiterea evenimentelor către un cluster
 * Apache Kafka. Implementează șablonul de proiectare Singleton pentru a
 * asigura existența unei singure instanțe a producătorului (producer) la
 * nivelul întregii aplicații, optimizând astfel conexiunile și utilizarea
 * resurselor de rețea.
 * @author Echipa Robotic Factory
 */
public final class KafkaProducerService {

    /** Instanța unică, partajată, a serviciului KafkaProducerService. */
    private static KafkaProducerService instance;

    /** Producătorul nativ Apache Kafka utilizat pentru expedierea
     * mesajelor. */
    private KafkaProducer<String, String> producer;

    /** Mapper-ul Jackson utilizat pentru serializarea obiectelor Java
     * în format text JSON. */
    private final ObjectMapper objectMapper;

    /** Numele topicului Kafka pe care se publică evenimentele fabricii. */
    private static final String TOPIC = "factory-events";

    /** Indicator stării de oprire a serviciului.
     * Este marcat ca {@code volatile} pentru a garanta vizibilitatea stării
     * actualizate între thread-uri.
     */
    private volatile boolean closed = false;

    /**
     * Constructor privat conform șablonului Singleton.
     * Configurează proprietățile producătorului Kafka, inclusiv
     * timeout-urile stricte pentru a preveni blocarea pe termen lung
     * în cazul în care broker-ul este inaccesibil.
     */
    private KafkaProducerService() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
        props.put("max.block.ms", "3000");
        props.put("request.timeout.ms", "3000");
        props.put("delivery.timeout.ms", "5000");
        this.producer = new KafkaProducer<>(props);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Returnează instanța unică a acestui serviciu.
     * Metoda este sincronizată ({@code synchronized}) pentru a asigura
     * crearea în siguranță a obiectului (Thread-Safe) în medii
     * multithreading.
     *
     * @return instanța unică de tip {@link KafkaProducerService}
     */
    public static synchronized KafkaProducerService getInstance() {
        if (instance == null) {
            instance = new KafkaProducerService();
        }
        return instance;
    }

    /**
     * Trimite un eveniment în mod sincron către broker-ul Kafka.
     * Metoda serializează obiectul {@link RobotEvent} în JSON și folosește
     * apelul {@code .get()} pentru a bloca firul de execuție curent până
     * când broker-ul confirmă (ack) recepția mesajului. În cazul în care
     * serviciul a fost deja închis, apelul este ignorat în mod silențios.
     *
     * @param inputEvent obiectul {@link RobotEvent} care conține detaliile
     *                   ce trebuie trimise
     */
    public void sendEvent(final RobotEvent inputEvent) {
        if (closed) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(inputEvent);
            ProducerRecord<String, String> record =
                new ProducerRecord<>(TOPIC, inputEvent.getRobotId(), json);
            producer.send(record).get();
        } catch (IllegalStateException e) {
            // producer inchis, ignoram
        } catch (Exception e) {
            System.err.println("Eroare trimitere event: " + e.getMessage());
        }
    }

    /**
     * Închide în mod securizat producătorul Kafka și eliberează toate
     * resursele logice active. Orice încercare ulterioară de a trimite
     * mesaje va fi respinsă prin verificarea flag-ului {@code closed}.
     */
    public synchronized void close() {
        if (!closed) {
            closed = true;
            producer.close();
        }
    }
}
