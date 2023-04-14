public class Task extends Thread {
    String name;
    int id, burst, burstCount = 0, wait = 0, arr;
    CPU cpu;
    public Task(int id){
        // create variable for number of CPU burst time
        // that tasks run for [1-50] : burst
        super(String.valueOf(id));
        this.id = id;
        this.name = "Thread " + id;
        this.burst = Use.randNum(1,50);
    }

    public Task(Task t){
        super(String.valueOf(t.id));
        this.id = t.id;
        this.name = t.name;
        this.burst = t.burst;
        this.burstCount = t.burstCount;

    }

    public void run(){
        while (burst > 0){

        }
    }

    public void setCPU(CPU cpu){
        this.cpu = cpu;

        wait += Scheduler.pc - arr;

        Use.print(
                name,
                // Max Burst
                " MB=" + burst +
                        // Current Burst
                        " CB=" + burstCount +
                        // Burst Target
                        " BT=" + (burst - burstCount) +
                        // Burst Goal
                        " BG=" + (burstCount + DC.q)
        );
    }
}
