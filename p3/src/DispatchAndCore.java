import java.util.ArrayList;
import java.util.concurrent.Semaphore;

// Code by Milan Haydel C00419477
class DC extends Thread {
    /*
     * C : Cores
     * Q : Quantum
     * P : PSJF (uses shorter burst time)
     */

    String disName;
    static int id, q;
    boolean p;

    public DC(int C, int Q, boolean P) {
        super(String.valueOf(C));
        this.id = C;
        this.disName = "Dispatcher " + id;
        this.q = Q;
        this.p = P;
    }

    public void run() {
        try {
            Scheduler.rMtx.acquire();
            Use.print(
                    disName,
                    "Using CPU " + id
            );

            Scheduler.rMtx.release();

            int i = 0;

            while (Scheduler.tasksDone.get() != Scheduler.totalTasks) {
                // System.out.println("\n\n***queue.size() = " + Scheduler.queue.size() + "\n\n");
                if (Scheduler.queue.size() == 1) Scheduler.finishedTsks.acquire();
                // if it is empty, it needs to create the rest of the tasks
                if (Scheduler.cpu[i % Scheduler.cpu.length].mtx.tryAcquire()) {
                    CPU cpu = Scheduler.cpu[i % Scheduler.cpu.length];

                    try {
                        Scheduler.qMtx.acquire();
                        Scheduler.rMtx.acquire();
                        Task t = Scheduler.queue.remove(0);
                        // if (!p) Scheduler.qMtx.release();
                        // hold the qmtx and release it after each burst

                        System.out.println();
                        Use.print(
                                disName,
                                "Running Process " + t.id
                        );
                        load(cpu, t);
                    } catch (IndexOutOfBoundsException e) {
                    }
                    Scheduler.cpu[i % Scheduler.cpu.length].mtx.release();
                }
                i++;

            }
        } catch (Exception e) {
            System.out.println(
                    "\n\n****************************************************" +
                            "\nSomething went wrong: " +
                            "\n" + e +
                            "\n****************************************************"
            );
        }
    }

    public void load(CPU cpu, Task t) {
        int bursts = calculateBurst(t, q);
        t.setCPU(cpu, bursts);
        System.out.println("\npassed1");

        if (!p) {
            t = cpu.burst(t, bursts);
            // Scheduler.rMtx.release();


        }
        // this is what p determines if true (only true for PSJF)
        // AKA task should preempt the currently running task if its
        // burst time is shorter than the burst time of the task that
        // is currently running
        // *** runs one burst at a time
        else {
            if (Scheduler.totalTasks > (Scheduler.tasksDone.get() + 1)) {
                while (t.burstCount < t.burst) {
                    //System.out.println("\npassed2");
                    if (t.burst - t.burstCount
                            <= Scheduler.queue.get(0).burst - Scheduler.queue.get(0).burstCount ||
                            Scheduler.queue.size() == 0) {
                        //System.out.println("\tpassed4");
                        //System.out.println("\nnext in queue id: " + Scheduler.queue.get(0).id);
                        t = cpu.burst(t, 1);

                        /*
                         * if after a completed burst there is still
                         * bursts to complete, it will acqiure the
                         * semaphore and wait

                        try {
                            if (t.burstCount != t.burst) {
                                Scheduler.qMtx.acquire();
                                Scheduler.rMtx.acquire();
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }*/
                        // Scheduler.rMtx.release();
                        // Scheduler.qMtx.release();
                    } else {
                        cpu.interrupt();
                        Use.print(
                                disName,
                                "Kicked Task " + t.id
                        );
                        System.out.println("\nNext in queue id: " +
                                Scheduler.queue.get(0).name +
                                "\nMaxBurst: " + Scheduler.queue.get(0).burst +
                                "\nCurrentBurst: " + Scheduler.queue.get(0).burstCount +
                                "\nAfter: " + Scheduler.queue.get(1).name);
                        Scheduler.rMtx.release();
                        Scheduler.qMtx.release();
                        break;
                    }
                }
            } else {
                // System.out.println("\n\n***taskCount == totalTasks***\n\n");
                t = cpu.burst(t, t.burst - t.burstCount);
            }
            /*
             * if the task has not completed its
             * max bursts, add the tasks to the
             * end of the task ready queue
             */
            if (t.burstCount != t.burst) {
                try {
                    Scheduler.qMtx.acquire();
                    // t.arr = Scheduler.pc;
                    Scheduler.queue.add(t);
                    Scheduler.qMtx.release();
                    Scheduler.sortQueue();

                    // Scheduler.rMtx.release();
                    // Scheduler.printQueue();
                } catch (Exception e) {
                }
            }
        }
    }

    public int calculateBurst(Task t, int quan) {
        switch (quan) {
            case 0:
                return t.burst - t.burstCount;
            default:
                if (t.burst - t.burstCount < quan) {
                    return t.burst - t.burstCount;
                }
                return quan;
        }
    }

    public void kick(CPU cpu) {
        cpu.interrupt();
    }
}

// Code by Milan Haydel C00419477
class CPU {
    String name;
    int id;
    Semaphore mtx = new Semaphore(1), cc = new Semaphore(0);
    boolean gtg = true;

    public CPU(int id) {
        this.id = id;
        this.name = "CPU " + id;

    }

    public Task burst(Task t, int bursts) {
        try {
            while (bursts-- > 0 && gtg) {
                // cc is acquired in task run()
                this.cc.release(1);
                t.start();
                t.join();
                t = new Task(t);
            }

            gtg = true;
        } catch (Exception e) {
            System.out.println(
                    "\n\n****************************************************" +
                            "\nSomething went wrong with " + name + ":" +
                            "\n" + e +
                            "\n****************************************************"
            );
        }

        return t;
    }

    public void interrupt() {
        this.gtg = false;
    }
}