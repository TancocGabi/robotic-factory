# 🏭 Robotic Factory Simulator
 
O aplicație Java de simulare a unei fabrici robotizate, cu interfață grafică JavaFX și monitorizare în timp real prin Apache Kafka.

 
## 📋 Descriere
 
**Robotic Factory Simulator** simulează funcționarea unei fabrici automatizate în care roboți de două tipuri — **procesare** și **transport** — colaborează pentru a executa task-uri industriale (sudură, asamblare, vopsire etc.). Activitatea roboților este monitorizată în timp real prin evenimente publicate și consumate via Apache Kafka.
 


## 📦 Clase principale
 
### `FactoryGUI`
Punctul de intrare al aplicației JavaFX. Gestionează:
- Fereastra de configurare a simulării (nr. roboți, tipuri de task-uri)
- Interfața principală de monitorizare cu reprezentare vizuală a roboților
- Zona de log pentru evenimentele Kafka
- Butoanele de control (Stop / Reset)
### `SimulationEngine`
Motorul de simulare multi-threaded. Responsabilități:
- Distribuie task-urile dinamic către roboții de procesare disponibili (busy-wait cu pauze de 100ms)
- Fiecare task rulează pe un thread propriu
- După procesare, un robot de transport preia produsul și îl duce în depozit
- Publică evenimente Kafka la fiecare schimbare de stare
### `Factory`
Container simplu pentru colecția de roboți. Oferă metode de adăugare și interogare a listei de roboți.
 
### `Robot`
Entitatea centrală a simulării. Caracteristici:
- Două tipuri: `"processing"` (execută task-uri) și `"finalizer"` (transportă produse)
- Stare thread-safe prin metode `synchronized`: `tryAcquire()` și `release()`
- Actualizare vizuală automată pe JavaFX Application Thread via `Platform.runLater()`
### `Task`
DTO (Data Transfer Object) simplu care încapsulează numele și durata unui task industrial.
 
### `RobotEvent`
DTO (Data Transfer Object) utilizat pentru transferul evenimentelor. Conține: `robotId`, `eventType`, `taskName`, `details`, `timestamp`. Compatibil cu serializarea Jackson JSON.
 
### `KafkaProducerService`
Singleton thread-safe care publică evenimente pe topicul `factory-events`. Trimitere sincronă (`.get()`) cu timeout-uri configurate pentru a evita blocaje.
 
### `KafkaConsumerService`
Serviciu asincron care ascultă topicul Kafka pe un thread daemon și notifică interfața grafică prin callback (`EventListener`).
 
 
## 🔄 Fluxul unui task
 
1. SimulationEngine găsește un Robot [processing] liber → tryAcquire()  
2. Publică eveniment TASK_STARTED pe Kafka  
3. Robot execută task (Thread.sleep(duration))  
4. Publică eveniment TASK_COMPLETED  
5. SimulationEngine găsește un Robot [finalizer] liber → tryAcquire()  
6. Publică eveniment PRODUCT_MOVED  
7. Robot transport simulează deplasarea (1000ms)  
8. Publică eveniment TASK_FINALIZED  
9. Ambii roboți sunt eliberați → release()  

## 👥 Autori
 
**Echipa Robotic Factory**

Țancoc Marian Gabriel  
Todireasa Constantin-Cătălin  
Știrbu Ștefan  
Păduraru Bogdan-Mihai  

Universitatea Tehnică „Gheorghe Asachi" din Iași, Facultatea de Automatică și Calculatoare
