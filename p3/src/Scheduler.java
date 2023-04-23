import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

// Code by Milan Haydel C00419477 and Chris Walther C00408978
public class Scheduler {
    // Code from Milan Haydel C00419477 ---
    String name = "Main Thread";
    static Semaphore qMtx = new Semaphore(1),
            cMtx = new Semaphore(1),
            rMtx = new Semaphore(1),
            createTsks = new Semaphore(0),
            none = new Semaphore(0);
    // qMtx used in createTasks for the queue
    // cMtx used in Task

    static ArrayList<Task> queue = new ArrayList<>();
    // ArrayList for ready queue to add task threads to
    static ArrayList<DC> dc = new ArrayList<>();
    static CPU[] cpu;
    static int S, taskCount = 0, totalTasks = 0;
    /*
     taskCount keeps track of what task ID to make for
     instances where new tasks are made at different
     times. This will be used (and only for) PSJF
     */
    static AtomicInteger tasksDone = new AtomicInteger(0);
    // ---


    // Code from Chris Walther C00408978 ---
    boolean randomTasks = true; // Set to false for handling Task 1 Question 1 and set to true standardly
    // ---

    // Done by Milan Haydel C00419477
    public Scheduler(int S, int Q, int C){
        this.S = S;
        cpu = new CPU[C];
        for(int i = 0; i < C; i++){
            cpu[i] = new CPU(i);
        }

        if (randomTasks){
            totalTasks = Use.randNum(1,25);
        } else {
            totalTasks = 5;
            Q = 5;
        }
        // 'Q' Quantum variable is only used for RR (case 2)
        switch (S){
            case 1:
                FCFS(C);
                break;
            case 2:
                RR(C, Q);
                break;
            case 3:
                NPSJF(C);
                break;
            case 4:
                PSJF(C);
                break;
        }
    }

    // Code by Milan Haydel C00419477 and Chris Walther C00408978
    public void FCFS(int c){
        if (randomTasks){createTasks(totalTasks, false);} else {createTasks(5, false);}
        printQueue();
        forking(c, 0, false);
    }

    // Code by Milan Haydel C00419477 and Chris Walther C00408978
    private void RR(int c, int q) {
        if (randomTasks){createTasks(totalTasks, false);} else {createTasks(5, false);}
        printQueue();
        forking(c, q, false);
    }

    // Code by Milan Haydel C00419477 and Chris Walther C00408978
    private void NPSJF(int c) {
        // Chris Walther C00408978 ---
        if (randomTasks){createTasks(totalTasks, false);} else {createTasks(5, false);}
        //printQueue(); //Temporary to test output
        try {
            sortQueue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        printQueue();
        // ---
        forking(c, 0, false);
    }

    // Code by Milan Haydel C00419477
    // Revised by Chris Walther C00408978
    private void PSJF(int c) {
        /*
         * The number of tasks for PSJF specifically (and only)
         * will be at least the same number of CPU cores and
         * most 10 tasks, for now, until after the threads (c, 10)
         * have started. Then a new set of tasks (1-15) will be
         * created and added to the queue. This is in order to
         * fulfill the requirement 'you must have tasks arriving
         * after threads have already started running on the CPU'.
         * The new set of tasks (1-15) is the other half of the
         * tasks range: [1-25].
         */
        // Code by Chris Walther C00408978 ---
        // Revised by Milan Haydel C00419477
        if (randomTasks){
            int m = Use.randNum(c,10);
            totalTasks = m;
            createTasks(m, false);
        } else {createTasks(1, false); totalTasks = 1;}
        int n;
        if (randomTasks){
            n = Use.randNum(1, 15);
            totalTasks = totalTasks + n;
        } else {n = 4; totalTasks = totalTasks + n;}
        try {
            sortQueue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        printQueue();
        System.out.println("Total Threads that need to be created: " + totalTasks);
        // ---
        forking(c, 0, true);
        /*
         * if boolean 'p' is true (only true for PSJF) it is because
         * task should preempt the currently running task if its
         * burst time is shorter than the burst time of the task that
         * is currently running (runs one burst at a time)
         */
        /*
         * The new set of tasks (1-15) added is the other half of the
         * tasks required range: [1-25].
         */

        // Code by Milan Haydel c00419477 ---
        while(n-- > 0){
            try {
                createTsks.acquire();
                createTasks(1, true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // ---
    }


    // Begin Code changes by Milan Haydel C00419477
    // Revised by Chris Walther C00408978

    // class to create tasks for each implementation and adds
    // them to an ArrayList<Task> 'ready' queue and uses Semaphores
    // [1-25].
    // The number of tasks will be different with PSJF because
    // you must have tasks arriving after threads
    // have already started running on the CPU

    public void createTasks(int tNum, boolean n){
        // System.out.print("\nCreating " + tNum + " task(s)..");
        for (int i = 0; i < tNum; i++){
            try {
                Task t = new Task(taskCount);
                // Code by Chris Walther C00408978 ---
                if (!randomTasks) {
                    if (taskCount == 0) {
                        t.burst = 18;
                    } else if (taskCount == 1) {
                        t.burst = 7;
                    } else if (taskCount == 2) {
                        t.burst = 25;
                    } else if (taskCount == 3) {
                        t.burst = 42;
                    } else {
                        t.burst = 21;
                    }
                }
                // ---

                if (n){
                    none.acquire();
                    DC.none.acquire();
                    qMtx.acquire();
                    queue.add(t);
                    qMtx.release();
                    sortQueue();
                    //Use.print(name, "Creating thread " + taskCount);
                    printQueue();
                    DC.preempTask.getAndDecrement();
                    DC.none.release();
                }
                else {
                    qMtx.acquire();
                    queue.add(t);
                    qMtx.release();
                    Use.print(name, "Creating thread " + taskCount);
                }

                taskCount++;
            } catch (Exception e) {}
        }
    }
    // End Code changes by Milan Haydel C00419477

    // Begin Code changes by Milan Haydel C00419477
    public static void printQueue(){
        try {
            rMtx.acquire();
            try {
                qMtx.acquire();
                System.out.print(
                        "\n\n--------------------Ready Queue---------------------"
                );
                for (Task t : queue) {
                    System.out.printf(
                            "\nID:%2s, Max Burst:%2d, Current Burst:%2d",
                            t.id, t.burst, t.burstCount
                    );
                }
                System.out.println(
                        "\n----------------------------------------------------"
                );
                qMtx.release();
            } catch (Exception e) {
                System.out.println();
            }
            rMtx.release();
        }
        catch (Exception e) {
            System.out.println();
        }
    }
    // End Code changes by Milan Haydel C00419477

    // Begin Code changes by Milan Haydel C00419477
    public void forking(int c, int q, boolean p){
        for (int i = 0; i < c; i++){
            DC d = new DC(i, q, p);
            Use.print(name, "Forking dispatcher " + i);
            dc.add(d);
            d.start();
        }
    }
    // End Code changes by Milan Haydel C00419477

    // Done by Chris Walther C00408978
    // This method sorts the order of queue by descending order from the shortest burst time to longest.
    public static void sortQueue() throws InterruptedException {
        try {
            qMtx.acquire();
            for(int i = 0; i < queue.size() - 1; i++){
                for(int j = i + 1; j < queue.size(); j++){
                    if(
                            queue.get(i).burst - queue.get(i).burstCount
                                    > queue.get(j).burst - queue.get(j).burstCount
                    ){
                        Task t = queue.get(i);
                        queue.set(i, queue.get(j));
                        queue.set(j, t);
                    }
                }
            }
            qMtx.release();
        } catch (Exception e) {
        }

        //-- End of sortQueue
    }
}
