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
        System.out.println("!!! COMANDA DE OPRIRE PRIMITA !!!");
    }

	public void start() {
	    List<Robot> allRobots = factory.getRobots();
	    
	    // Separăm listele pentru a gestiona pool-urile de resurse
	    List<Robot> processingPool = allRobots.stream().filter(r -> r.getType().equals("processing"))
	            .toList();
	            
	    List<Robot> transportPool = allRobots.stream().filter(r -> r.getType().equals("finalizer"))
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
	                
	                System.out.println("[PRODUCTIE] " + worker.getId() + " proceseaza " + task.getName());
	                Thread.sleep(task.getDuration());

	                // Căutăm un RT, dar ieșim dacă simularea se oprește
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
	                    System.out.println("[TRANSPORT] " + selectedTransport.getId() + " preia piesa de la " + worker.getId());
	                    
	                    selectedTransport.setCurrentTaskName("De la " + worker.getId());
	                    selectedTransport.updateVisuals();
	                    
	                    Thread.sleep(1000); // Timp transport

	                    selectedTransport.release();
	                    selectedTransport.setCurrentTaskName("Inactiv");
	                    selectedTransport.updateVisuals();
	                    
	                    System.out.println("[FINALIZAT] Task-ul " + task.getName() + " a fost depozitat.");
	                } else {
	                    // Dacă am ajuns aici pentru că s-a apăsat STOP
	                    System.out.println("[STOP] " + worker.getId() + " a abandonat task-ul " + task.getName() + " din cauza opririi.");
	                }

	                // Eliberăm mereu worker-ul, indiferent dacă transportul s-a făcut sau nu
	                worker.release();
	                worker.setCurrentTaskName("Inactiv");
	                worker.updateVisuals();

	            } catch (InterruptedException e) {
	                worker.release();
	                System.err.println("Thread intrerupt pentru " + worker.getId());
	            }
	        }).start();
	        
	    }
	}
}