# CPU Scheduling in Single Core and Multi-Core Systems
Project 3

1. Implementation of first-come, first-served (FCFS); round-robin (RR); non-preemptive shortest job first (NSJF); and preemptive shortest job first (PSJF) algorithms for a single-core processor
2. Implementation of FCFS, RR, and NSJF for a multi-core processor

Creates a total of T task threads, forks them, and adds them to a ready queue. Each task is modeled by a thread that runs for a total CPU burst time of B cycles before exiting. Each thread implements a loop, where each iteration of the loop represents one CPU burst cycle. After populating the ready queue, forks a dispatcher thread which selects a task thread from the ready queue and allows it to run on the CPU for a specified time. For each run, the program dynamically generates a value of T in the range [1,25] and a value of B in the range [1,50].
