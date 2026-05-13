package ro.tuiasi.ac.robotic_factory;

import org.junit.jupiter.api.Test;
import org.apache.kafka.clients.admin.AdminClient;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    private Properties getProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("request.timeout.ms", "3000");
        props.put("connections.max.idle.ms", "3000");
        props.put("default.api.timeout.ms", "3000");
        return props;
    }

    @Test
    void kafkaBrokerShouldBeRunning() {
        try (AdminClient client = AdminClient.create(getProps())) {
            client.describeCluster().nodes().get(3, TimeUnit.SECONDS);
            System.out.println("Kafka este pornit.");
        } catch (Exception e) {
            System.out.println("Kafka NU este pornit.");
            fail("Kafka NU este pornit pe localhost:9092");
        }
    }

    @Test
    void factoryEventsTopicShouldExist() {
        try (AdminClient client = AdminClient.create(getProps())) {
            Set<String> topics = client.listTopics().names().get(3, TimeUnit.SECONDS);
            if (topics.contains("factory-events")) {
                System.out.println("Topic-ul factory-events exista.");
            } else {
                System.out.println("Topic-ul factory-events NU exista.");
                fail("Topic-ul factory-events nu a fost creat.");
            }
        } catch (Exception e) {

            System.out.println("Eroare la verificare topic.");
            fail("Eroare la conectare Kafka.");
        }
    }
}