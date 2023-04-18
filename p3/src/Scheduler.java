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
            finishedTsks = new Semaphore(0);;
    // qMtx used in createTasks for the queue
    // cMtx used in Task

    static ArrayList<Task> queue = new ArrayList<>();
    // ArrayList for ready queue to add task threads to
    static ArrayList<DC> dc = new ArrayList<>();
    static CPU[] cpu;
    static int taskCount = 0, totalTasks = 0;
    static AtomicInteger tasksDone = new AtomicInteger(0);
    /*
     taskCount keeps track of what task ID to make for
     instances where new tasks are made at different
     times. This will be used (and only for) PSJF
     */
    // ---


    // Code from Chris Walther C00408978 ---
    boolean randomTasks = true; // Set to false for handling Task 1 Question 1 and set to true standardly
    // ---

    // Done by Milan Haydel C00419477
    public Scheduler(int S, int Q, int C){
        cpu = new CPU[C];
        for(int i = 0; i < C; i++){
            cpu[i] = new CPU(i);
        }

        if (randomTasks){
            totalTasks = Use.randNum(1,25);
        } else {totalTasks = 5;}
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
        } else {createTasks(3, false); totalTasks = totalTasks + 3;}
        int n;
        if (randomTasks){
            n = Use.randNum(1, 15);
            totalTasks = totalTasks + n;
        } else {n = 2; totalTasks = totalTasks + 2;}
        try {
            sortQueue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        printQueue();
        System.out.println("Total Threads: " + totalTasks);
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

        while(n-- > 0){
            createTasks(1, true);
            // Code by Chris Walther C00408978 ---
            /*try {
                sortQueue();
                printQueue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
            // ---
        }
    }


    // Code by Milan Haydel C00419477
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

                qMtx.acquire();
                queue.add(t);
                qMtx.release();

                if (n){
                    sortQueue();
                    printQueue();
                    Use.print(name, "Creating thread " + taskCount);
                }
                else Use.print(name, "Creating thread " + taskCount);

                taskCount++;
                if (taskCount == totalTasks && n) finishedTsks.release();
            } catch (Exception e) {}
        }
    }

    // Code by Milan Haydel C00419477
    public static void printQueue(){
        try {
            rMtx.acquire();
            System.out.print(
                    "\n\n--------------------Ready Queue---------------------"
            );
            try {
                qMtx.acquire();
                for (Task t : queue) {
                    System.out.printf(
                            "\nID:%2s, Max Burst:%2d, Current Burst:%2d",
                            t.id, t.burst, t.burstCount
                    );
                }
                qMtx.release();
            } catch (Exception e) {
                System.out.println();
            }
            System.out.println(
                    "\n----------------------------------------------------"
            );
            rMtx.release();
        }
        catch (Exception e) {
            System.out.println();
        }
    }

    // Code by Milan Haydel C00419477
    public void forking(int c, int q, boolean p){
        for (int i = 0; i < c; i++){
            DC d = new DC(i, q, p);
            Use.print(name, "Forking dispatcher " + i);
            dc.add(d);
            d.start();
        }
    }

    // Done by Chris Walther C00408978
    // This method sorts the order of queue by descending order from the shortest burst time to longest.
    public static void sortQueue() throws InterruptedException {
        //while (queue.size() > 0) {
        try {
            qMtx.acquire();
            Collections.sort(queue);
            qMtx.release();
        } catch (Exception e) {}
        //}

        //-- End of sortQueue
    }
}
