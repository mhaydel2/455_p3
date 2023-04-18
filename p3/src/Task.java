// Code by Milan Haydel C00419477
// Revised by Patrick Leleux

public class Task extends Thread implements Comparable<Task> {
    String name;
    int id, burst, burstCount = 0;
    CPU cpu;
    public Task(int id){
        // create variable for number of CPU burst time
        // that tasks run for [1-50] : burst
        super(String.valueOf(id));
        this.id = id;
        this.name = "Proc. Thread " + id;
        this.burst = Use.randNum(1,50);
    }

    public Task(Task t){
        super(String.valueOf(t.id));
        this.id = t.id;
        this.name = t.name;
        this.burst = t.burst;
        this.burstCount = t.burstCount;
        this.cpu = t.cpu;

    }

    // Done by Chris Walther C00408978 ---
    public int getBurst() {
        return burst;
    }

    public int getBurstCount() {
        return burstCount;
    }
    /*
    public long getEndTime() {
        long end = System.currentTimeMillis();
        System.out.println("Timer end");
        return end;
    }
     */

    @Override
    public int compareTo(Task other) {
        return this.burst - other.getBurst() - other.getBurstCount();
    }  //-1 if other > this, 0 if other = this, 1 if other < this

    // ---

    public void run(){
        // use a try catch statement if you want with the while loop inside.

        // use the CPU.cc Semaphore in the while loop (in addition to other things).
        // the cc Semaphore is only used for bursts
        // it is released in CPU.burst and acquired in this
        // while loop before completing burst(s)
        try{

        while (this.burstCount < this.burst && this.cpu.cc.tryAcquire()){

            // use the cMtx Semaphore from Scheduler => Scheduler.cMtx.acquire.
            Scheduler.cMtx.acquire();
            Use.print(name, "Using "+this.cpu.name+"; On burst "+ ++this.burstCount);
            Scheduler.cMtx.release();
        }
        if (this.burstCount == this.burst){
            Scheduler.tasksDone.getAndIncrement();
        }
        } catch (Exception e) {}
        //getEndTime();
    }

    public void setCPU(CPU cpu, int bg){
        this.cpu = cpu;

        // wait += Scheduler.pc - arr;

        Use.print(
                name,
                // Max Burst
                "MB=" + burst +
                        // Current Burst
                        ", CB=" + burstCount +
                        // Burst Target
                        ", BT=" + bg +
                        // Burst Goal
                        ", BG=" + (bg + burstCount)
        );
    }
}
