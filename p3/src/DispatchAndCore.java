import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

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
    static Semaphore none = new Semaphore(1);
    static AtomicInteger preempTask = new AtomicInteger(0);

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
            Use.print(
                    disName,
                    Use.getAlg(Scheduler.S)
            );
            Scheduler.rMtx.release();
            int i = 0;

            while (Scheduler.tasksDone.get() != Scheduler.totalTasks) {
                Scheduler.rMtx.acquire();
                if (Scheduler.cpu[i % Scheduler.cpu.length].mtx.tryAcquire()) {
                    CPU cpu = Scheduler.cpu[i % Scheduler.cpu.length];

                    try {
                        Scheduler.qMtx.acquire();
                        Task t = Scheduler.queue.remove(0);
                        if (!p) {Scheduler.qMtx.release();
                        Scheduler.rMtx.release();}
                        else if (p && t.burstCount != 0){Scheduler.qMtx.release();
                            Scheduler.rMtx.release();
                        }
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
                    "\nSomething went wrong: " +
                            "\n" + e
            );
        }
    }

    public void load(CPU cpu, Task t) {
        int bursts = calculateBurst(t, q);
        t.setCPU(cpu, bursts);

        if (!p) {
            t = cpu.burst(t, bursts);
            if (t.burstCount != t.burst) {
                try {
                    Scheduler.qMtx.acquire();
                    // t.arr = Scheduler.pc;
                    Scheduler.queue.add(t);
                    Scheduler.qMtx.release();
                } catch (Exception e) {
                }
            }
        }
        // this is what p determines if true (only true for PSJF)
        // AKA task should preempt the currently running task if its
        // burst time is shorter than the burst time of the task that
        // is currently running
        // *** runs one burst at a time
        else {
            while (t.burstCount < t.burst){
                try {
                    none.acquire();
                    if (Scheduler.queue.size() != 0) {
                        if (t.burst - t.burstCount
                                <= Scheduler.queue.get(0).burst - Scheduler.queue.get(0).burstCount) {
                            // releases after the first burst
                            if (t.burstCount == 0) {
                                Scheduler.rMtx.release();
                                t = cpu.burst(t, 1);
                                Scheduler.qMtx.release();
                            } else t = cpu.burst(t, 1);
                        } else {
                            if (t.burstCount == 0) {
                                Scheduler.qMtx.release();
                                Scheduler.rMtx.release();
                            }
                            kickTask(cpu, t);
                            break;
                        }
                    } else {
                        if (t.burstCount == 0) {
                            Scheduler.qMtx.release();
                            Scheduler.rMtx.release();
                        }
                        if (Scheduler.queue.size() == 0) t = cpu.burst(t, 1);
                    }
                    if (Scheduler.createTsks.availablePermits() == 0) {
                        Scheduler.createTsks.release();
                    }
                    none.release();
                    if (Scheduler.none.availablePermits() == 0 && preempTask.get() == 0) {
                        Scheduler.none.release();
                        preempTask.getAndIncrement();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            none.release();
        }
    }

    public void kickTask(CPU cpu, Task t){
        try {
            Scheduler.rMtx.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        cpu.interrupt();
        Use.print(
                disName,
                "Kicked Task " + t.id
        );
        if (t.burstCount != t.burst) {
            try {
                System.out.println("\nSorting...");
                Scheduler.qMtx.acquire();
                Scheduler.queue.add(t);
                Scheduler.qMtx.release();
                Scheduler.sortQueue();
            } catch (Exception e) {}
        }
        System.out.println("\nNext in queue id: " +
                Scheduler.queue.get(0).name +
                "\nMaxBurst: " + Scheduler.queue.get(0).burst +
                "\nCurrentBurst: " + Scheduler.queue.get(0).burstCount +
                "\nAfter: " + Scheduler.queue.get(1).name);
        Scheduler.rMtx.release();
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