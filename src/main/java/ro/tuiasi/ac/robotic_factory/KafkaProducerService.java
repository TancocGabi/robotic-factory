package ro.tuiasi.ac.robotic_factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class KafkaProducerService {
    private static KafkaProducerService instance;
    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "factory-events";
    private volatile boolean closed = false;

    private KafkaProducerService() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("max.block.ms", "3000");
        props.put("request.timeout.ms", "3000");
        props.put("delivery.timeout.ms", "5000");
        this.producer = new KafkaProducer<>(props);
        this.objectMapper = new ObjectMapper();
    }

    public static synchronized KafkaProducerService getInstance() {
        if (instance == null) {
            instance = new KafkaProducerService();
        }
        return instance;
    }
// metoda asincrona
//    public void sendEvent(RobotEvent event) {
//        if (closed) return;
//        try {
//            String json = objectMapper.writeValueAsString(event);
//            ProducerRecord<String, String> record =
//                new ProducerRecord<>(TOPIC, event.getRobotId(), json);
//            producer.send(record, (metadata, exception) -> {
//                if (exception != null) {
//                    System.err.println("Eroare Kafka: " + exception.getMessage());
//                }
//            });
//        } catch (IllegalStateException e) {
//            // producer inchis, ignoram
//        } catch (Exception e) {
//            System.err.println("Eroare trimitere event: " + e.getMessage());
//        }
//    }
// metoda sincrona
    public void sendEvent(RobotEvent event) {
        if (closed) return;
        try {
            String json = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record =
                new ProducerRecord<>(TOPIC, event.getRobotId(), json);
            producer.send(record).get(); // <-- .get() face trimiterea sincrona
        } catch (IllegalStateException e) {
            // producer inchis, ignoram
        } catch (Exception e) {
            System.err.println("Eroare trimitere event: " + e.getMessage());
        }
    }
    
    public synchronized void close() {
        if (!closed) {
            closed = true;
            producer.close();
        }
    }
}