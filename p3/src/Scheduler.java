import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class Scheduler {
    String name = "Main Thread";
    // qMtx used in createTasks for the queue
    // cMtx used in Task
    static Semaphore qMtx = new Semaphore(1), cMtx = new Semaphore(1);

    // ArrayList for ready queue to add task threads to
    static ArrayList<Task> queue = new ArrayList<>();
    static ArrayList<DC> dc = new ArrayList<>();
    static CPU[] cpu;

    /*
     taskCount keeps track of what task ID to make for
     instances where new tasks are made at different
     times. This will be used (and only for) PSJF
     */
    /*
     * core_dis is just a count of how many CPU cores/
     * dispatchers we have in total to use in dispatcher
     */
    static int taskCount = 0;

    // NPSJF and PSJF will use a class that sorts the queue
    // of tasks

    // class that prints the ready queue

    // class to make dispatchers and CPUs

    public Scheduler(int S, int Q, int C){
        cpu = new CPU[C];
        // there is an additional semaphore is CPU labelled 'cc'
        // cpu mtx is used in dispatcher run and
        // cc mtx is used in task run
        for(int i = 0; i < C; i++){
            cpu[i] = new CPU(i);
        }
        // 'Q' Quantum is only used for RR (case 2)
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

    // 4 separate classes for FCFS, RR, NPSJF, PSJF
    public void FCFS(int c){
        createTasks(Use.randNum(1,25));
        printQueue();
        // call DC using # of cores 'c'
        // fork dispatcher
        forking(c, 0, false);
    }

    private void RR(int c, int q) {
        createTasks(Use.randNum(1,25));
        printQueue();
        // call DC using # of cores 'c' and quantum
        // fork dispatcher
        forking(c, q, false);
    }

    // Revised by Chris Walther C00408978
    private void NPSJF(int c) {
        createTasks(Use.randNum(1,25));
        printQueue();
        // Chris Walther C00408978 ---
        try {
            sortQueue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //printQueue(); //Temporary to test output
        // ---

        // call DC using # of cores 'c'
        // fork dispatcher
        forking(c, 0, false);
    }

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
        createTasks(Use.randNum(c, 10));
        //sortQueue();
        try {
            sortQueue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        printQueue();
        // call DC using # of cores 'c' and boolean p
        // fork dispatcher
        forking(c, 0, true); //Chris
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

        int n = Use.randNum(1, 15);
        while(n-- > 0){
            createTasks(1);
            //sortQueue
            try {
                sortQueue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            printQueue();
        }

    }


    // class to create tasks for each implementation and adds
    // them to an ArrayList<Task> 'ready' queue and uses Semaphores
    // [1-25].
    // The number of tasks will be different with PSJF because
    // you must have tasks arriving after threads
    // have already started running on the CPU

    public void createTasks(int tNum){
        System.out.println("Creating " + tNum + " task(s)..");
        for (int i = 0; i < tNum; i++){
            try {
                Task t = new Task(taskCount);
                qMtx.acquire();
                queue.add(t);
                qMtx.release();

                Use.print(name, "Creating thread " + taskCount);
                taskCount++;
            } catch (Exception e) {}
        }
    }

    public void printQueue(){
        System.out.print(
                "\n\n--------------------Ready Queue---------------------"
        );

        try {
            qMtx.acquire();
            for(Task t : queue){
                System.out.printf(
                        "\nID:%2s, Max Burst:%2d, Current Burst:%2d",
                        t.id, t.burst, t.burstCount
                );
            }
            qMtx.release();
        } catch (Exception e) {}
        System.out.println(
                "\n----------------------------------------------------"
        );
    }

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
    public void sortQueue() throws InterruptedException {
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
