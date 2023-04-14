import java.util.ArrayList;
import java.util.concurrent.Semaphore;

class DC extends Thread{
    /*
     * C : Cores
     * Q : Quantum
     * P : PSJF (uses shorter burst time)
     */

    // Dispatcher and Core in one thread

    String disName, cpuName;
    static int id, q;
    boolean p = false;

    public DC(int C, int Q, boolean P){
        super(String.valueOf(C));
        this.id = C;
        this.disName = "Dispatcher " + id;
        // this.cpuName = "CPU " + id;
        this.q = Q;
        this.p = P;
    }

    // depending on the selected algorithm,
    // select a task from a ready queue and
    // allow it to run on the CPU
    public void run(){
        try {
            int i = 0;

            while(Scheduler.queue.size() > 0){
                if(Scheduler.cpu[i % Scheduler.cpu.length].mtx.tryAcquire()){
                    CPU cpu = Scheduler.cpu[i % Scheduler.cpu.length];

                    try {
                        Scheduler.qMtx.acquire();
                        Task t = Scheduler.queue.remove(0);
                        Scheduler.qMtx.release();

                        Use.print(
                                disName,
                                "Loading Task " + t.id + " on CPU " + cpu.id +
                                        "\n----------------------------------------------------"
                        );
                        load(cpu, t);
                    } catch (IndexOutOfBoundsException e) {}
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

    public void load(CPU cpu, Task t){
        t.setCPU(cpu);

        if(!p){
            int bursts = calculateBurst(t, q);
            //t.wait += Scheduler.pc - t.arr;
            t = cpu.burst(t, bursts);

        }
        // this is what p determines if true (only true for PSJF)
        // AKA task should preempt the currently running task if its
        // burst time is shorter than the burst time of the task that
        // is currently running
        // *** runs one burst at a time
        else{
            if(Scheduler.queue.size() != 0){
                while(t.burst - t.burstCount
                                <= Scheduler.queue.get(0).burst - Scheduler.queue.get(0).burstCount
                                && t.burstCount < t.burst){
                    t = cpu.burst(t, 1);
                }

                if(t.burstCount != t.burst){
                    cpu.interrupt();
                    Use.print(
                            disName,
                            "Kicked Task " + t.id
                    );
                }

            }else{
                t = cpu.burst(t, t.burst - t.burstCount);
            }
        }

        if(t.burstCount != t.burst){
            try {
                Scheduler.qMtx.acquire();
                t.arr = Scheduler.pc;
                Scheduler.queue.add(t);
                Scheduler.qMtx.release();
            } catch (Exception e) {}
        }
    }

    public int calculateBurst(Task t, int quan){
        switch(quan){
            case 0:
                return t.burst - t.burstCount;
            default:
                if(t.burst - t.burstCount < quan){
                    return t.burst - t.burstCount;
                }
                return quan;
        }
    }

    public void kick(CPU cpu){
        cpu.interrupt();
    }
}

class CPU {
    String name;
    int id, history = -1;
    Semaphore mtx = new Semaphore(1), cc = new Semaphore(0);
    boolean gtg = true;

    public CPU(int id){
        this.id = id;
        this.name = "CPU " + id;

    }

    public Task burst(Task t, int bursts){
        try{
            while(bursts-- > 0 && gtg){
                // cc is acquired in task run()
                this.cc.release(1);
                t.start();
                t.join();
                t = new Task(t);
            }

            gtg = true;
        } catch(Exception e){
            System.out.println(
                    "\n\n****************************************************" +
                            "\nSomething went wrong with " + name + ":" +
                            "\n" + e +
                            "\n****************************************************"
            );
        }

        return t;
    }

    public void interrupt(){
        this.gtg = false;
    }
}