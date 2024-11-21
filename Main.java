import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

class Process {
    private int processId;
    private PriorityQueue<Message> queue;
    private Set<Acknowledgment> acknowledgments;
    private int eventCounter;

    public Process(int processId) {
        this.processId = processId;
        this.queue = new PriorityQueue<>();
        this.acknowledgments = new HashSet<>();
        this.eventCounter = 0;
    }

    public void multicast(String message, CommunicationThread[] threads) {
        this.eventCounter++;
        int eventId = this.eventCounter;
        for (int i = 0; i < threads.length; i++) {
            if (i != this.processId) {
                threads[i].queueMessage(this.processId, eventId, message);
            }
        }
    }

    public void receiveMessage(int senderId, int eventId, String message, CommunicationThread[] threads) {
        this.queue.add(new Message(eventId, senderId, message));
        this.multicastAck(eventId, threads);
    }

    private void multicastAck(int eventId, CommunicationThread[] threads) {
        for (int i = 0; i < threads.length; i++) {
            if (i != this.processId) {
                threads[i].queueAck(this.processId, eventId);
            }
        }
    }

    public void receiveAck(int senderId, int eventId) {
        this.acknowledgments.add(new Acknowledgment(eventId, senderId));
    }

    public void deliverMessages() throws InterruptedException {
        while (!this.queue.isEmpty()) {
            Message message = this.queue.poll();
            Acknowledgment acknowledgment = new Acknowledgment(message.getEventId(), message.getSenderId());
            if (this.acknowledgments.contains(acknowledgment)) {
                System.out.printf("%d: %d.%d: %s%n", this.processId, message.getEventId(), message.getSenderId(), message.getMessage());
                this.acknowledgments.remove(acknowledgment);
            }
        }
    }

    public int getProcessId() {
        return processId;
    }

    public int getEventCounter() {
        return eventCounter;
    }
}

class Message implements Comparable<Message> {
    private int eventId;
    private int senderId;
    private String message;

    public Message(int eventId, int senderId, String message) {
        this.eventId = eventId;
        this.senderId = senderId;
        this.message = message;
    }

    public int getEventId() {
        return eventId;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int compareTo(Message other) {
        return Integer.compare(this.eventId, other.eventId);
    }
}

class Acknowledgment {
    private int eventId;
    private int senderId;

    public Acknowledgment(int eventId, int senderId) {
        this.eventId = eventId;
        this.senderId = senderId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Acknowledgment that = (Acknowledgment) obj;
        return eventId == that.eventId && senderId == that.senderId;
    }

    @Override
    public int hashCode() {
        return 31 * eventId + senderId;
    }
}

class CommunicationThread extends Thread {
    private Process process;
    private CommunicationThread[] threads;
    private boolean exitEvent;

    public CommunicationThread(Process process, CommunicationThread[] threads) {
        this.process = process;
        this.threads = threads;
        this.exitEvent = false;
    }

    public void run() {
        while (!exitEvent) {
            try {
                Thread.sleep(2000);
                String message = String.format("Message from Process %d", this.process.getProcessId());
                int senderId = (this.process.getProcessId() + 1) % threads.length;
                this.process.multicast(message, threads); // Increment event counter internally
            } catch (InterruptedException e) {
                
                break;
            }
        }
    }

    public void queueMessage(int senderId, int eventId, String message) {
        this.process.receiveMessage(senderId, eventId, message, threads);
    }

    public void queueAck(int senderId, int eventId) {
        this.process.receiveAck(senderId, eventId);
    }
}

class DeliveryThread extends Thread {
    private Process process;
    private boolean exitEvent;

    public DeliveryThread(Process process) {
        this.process = process;
        this.exitEvent = false;
    }

    public void run() {
        while (!exitEvent) {
            try {
                this.process.deliverMessages();
                Thread.sleep(1000);  // Adjust the sleep duration as needed
            } catch (InterruptedException e) {
                // Handle the InterruptedException as needed
                break;
            }
        }
    }

    public void setExitEvent() {
        this.exitEvent = true;
    }
}

public class Main {
    private static final int NUM_PROCESSES = System.getenv("NUM_PROCESSES") != null ?
            Integer.parseInt(System.getenv("NUM_PROCESSES")) : 3;

    private static CommunicationThread[] threads;
    private static Process[] processes;
    private static DeliveryThread[] deliveryThreads;

    public static void main(String[] args) {
        if (NUM_PROCESSES >= 3) {
            processes = new Process[NUM_PROCESSES];
            threads = new CommunicationThread[NUM_PROCESSES];
            deliveryThreads = new DeliveryThread[NUM_PROCESSES];

            for (int i = 0; i < NUM_PROCESSES; i++) {
                processes[i] = new Process(i);
                threads[i] = new CommunicationThread(processes[i], threads);
                threads[i].start();
                deliveryThreads[i] = new DeliveryThread(processes[i]);
                deliveryThreads[i].start();
            }

            try {
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Simulation terminated.");
                for (CommunicationThread thread : threads) {
                    thread.interrupt();
                }
                for (DeliveryThread thread : deliveryThreads) {
                    thread.setExitEvent();
                    thread.interrupt();
                }
            }
        } else {
            System.out.println("the no.of processes should be >= 3");
        }
    }
}