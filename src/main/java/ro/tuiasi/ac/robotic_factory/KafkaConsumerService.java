package ro.tuiasi.ac.robotic_factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerService {
    private KafkaConsumer<String, String> consumer;
    private ObjectMapper objectMapper;
    private EventListener listener;
    private Thread consumerThread;
    private volatile boolean ready = false;

    public interface EventListener {
        void onEvent(RobotEvent event);
    }

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

    public void startListening() {
        consumerThread = new Thread(() -> {
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
                    break;
                }
            }
            Thread.interrupted();
            consumer.close();
        });
        consumerThread.start();
    }

    public void waitUntilReady() throws InterruptedException {
        while (!ready) {
            Thread.sleep(100);
        }
    }

    public void stop() {
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }
}