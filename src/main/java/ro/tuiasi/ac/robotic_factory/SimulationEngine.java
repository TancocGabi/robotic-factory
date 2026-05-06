package ro.tuiasi.ac.robotic_factory;

import java.util.List;

public class SimulationEngine {
	private Factory factory;
	private List<Task> tasks;
	// Flag pentru oprire
    private volatile boolean running = true;

	public SimulationEngine(Factory factory, List<Task> tasks) {
		this.factory = factory;
		this.tasks = tasks;
	}
	
	public void stop() {
        this.running = false;
        System.out.println("!!! COMANDĂ DE OPRIRE PRIMITĂ !!!");
    }

	public void start() {
	    List<Robot> allRobots = factory.getRobots();
	    
	    // Separăm listele pentru a gestiona pool-urile de resurse
	    List<Robot> processingPool = allRobots.stream()
	            .filter(r -> r.getType().equals("processing"))
	            .toList();
	            
	    List<Robot> transportPool = allRobots.stream()
	            .filter(r -> r.getType().equals("finalizer"))
	            .toList();

	    System.out.println("--- START SIMULARE: " + tasks.size() + " task-uri ---");

	    for (Task task : tasks) {
	    	
	    	// Dacă s-a apăsat STOP, nu mai luăm task-uri noi
            if (!running) break;
            
	        Robot selectedWorker = null;

	        // Pasul 1: Așteptăm un robot de procesare liber
	        while (selectedWorker == null && running) {
	            for (Robot r : processingPool) {
	                if (r.tryAcquire()) {
	                    selectedWorker = r;
	                    break;
	                }
	            }
	            if (selectedWorker == null) {
	                try { Thread.sleep(100); } catch (Exception e) {return;}
	            }
	        }
	        
	        if (selectedWorker == null) continue;
	        
	        final Robot worker = selectedWorker;
	        worker.setCurrentTaskName(task.getName());
	        worker.updateVisuals();

	        new Thread(() -> {
	            try {
	            	if (!running) { worker.release(); return; }
	            	
	                // ETAPA DE PROCESARE
	                System.out.println("[PRODUCTIE] " + worker.getId() + " a inceput task-ul: " + task.getName());
	                
	                Thread.sleep(task.getDuration());
	                
	                System.out.println("[WAITING] " + worker.getId() + " a terminat " + task.getName() + " si asteapta transportul.");

	                // Pasul 2: Căutăm un robot de transport disponibil
	                Robot selectedTransport = null;
	                while (selectedTransport == null && running) {
	                    for (Robot rt : transportPool) {
	                        if (rt.tryAcquire()) {
	                            selectedTransport = rt;
	                            break;
	                        }
	                    }
	                    if (selectedTransport == null) {
	                        Thread.sleep(100); // Așteptăm să se elibereze un RT
	                    }
	                }

	                // ETAPA DE TRANSPORT
	                final Robot transportBot = selectedTransport;
	                System.out.println("[TRANSPORT] " + transportBot.getId() + " preia piesa de la " + worker.getId());
	                
	                transportBot.setCurrentTaskName("Transport de la " + worker.getId());
	                transportBot.updateVisuals();
	                
	                Thread.sleep(1000); // Durata transportului către depozit

	                // FINALIZARE: Eliberăm ambii roboți
	                worker.release();
	                worker.setCurrentTaskName("Inactiv");
	                worker.updateVisuals();

	                transportBot.release();
	                transportBot.setCurrentTaskName("Inactiv");
	                transportBot.updateVisuals();
	                
	                System.out.println("[FINALIZAT] Task-ul " + task.getName() + " a fost depozitat de " + transportBot.getId());

	            } catch (InterruptedException e) {
	            	worker.release();
	                System.err.println("Eroare în thread-ul robotului " + worker.getId());
	                e.printStackTrace();
	            }
	        }).start();
	    }
	}
}