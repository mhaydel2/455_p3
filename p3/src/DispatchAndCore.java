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
    int id, q;
    boolean p = false;

    public DC(int C, int Q, boolean P){
        super(String.valueOf(C));
        this.id = C;
        this.disName = "Dispatcher " + id;
        this.cpuName = "CPU " + id;
        this.q = Q;
        this.p = P;
    }

    // depending on the selected algorithm,
    // select a task from a ready queue and
    // allow it to run on the CPU
    public void run(){

    }
}