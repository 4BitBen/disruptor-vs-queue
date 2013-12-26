# Overview
This repository is meant as a playground for working with a Disruptor Ring Buffer vs a Blocking (aka Bounded) Queue.
The hope is to better understand latencies, resource utilization, configuration of each approach as well as get an easy
first glimpse of how your business logic will run with either approach.

# Code Layout
*\<top\>* - Interfaces to state how you want to produce and consume events and well as an interface for creating events.

*queue* - Classes for running the Blocking Queue.

*disruptor* - Classes for running the Disruptor Ring Buffer. There are two approaches: EventHandler and WorkHandler.
The former behaves more like a topic where all consumers receive the same message and must filter out the ones they
don't care about. The latter behaves more like a queue where only one consumer receives a message and the load is
balanced between all consumers.

*sample* - Classes for running a using two simple examples: a no-op which takes the event and throws it on the floor
and a print which logs the event.

# TODO
1. Have tests to verify correctness - all workers get only one message, all messages seen.

# Performance
The following is the output of running on Windows 7, i7 CPU. A total of 8 CPUs.

## No Op Producer

### Single Thread

50M messages
12/26/13 3:58:10 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 50000000
         mean rate = 847451.61 calls/second
     1-minute rate = 828785.37 calls/second
     5-minute rate = 805020.62 calls/second
    15-minute rate = 799389.65 calls/second
               min = 888.44 microseconds
               max = 2263.97 microseconds
              mean = 1198.77 microseconds
            stddev = 146.30 microseconds
            median = 1198.96 microseconds
              75% <= 1295.84 microseconds
              95% <= 1422.28 microseconds
              98% <= 1457.56 microseconds
              99% <= 1508.08 microseconds
            99.9% <= 2257.82 microseconds
queue.consume
             count = 50000000
         mean rate = 847396.18 calls/second
     1-minute rate = 828784.07 calls/second
     5-minute rate = 805018.08 calls/second
    15-minute rate = 799386.81 calls/second
               min = 0.00 microseconds
               max = 0.51 microseconds
              mean = 0.01 microseconds
            stddev = 0.07 microseconds
            median = 0.00 microseconds
              75% <= 0.00 microseconds
              95% <= 0.00 microseconds
              98% <= 0.51 microseconds
              99% <= 0.51 microseconds
            99.9% <= 0.51 microseconds
queue.dequeue
             count = 50000001
         mean rate = 847352.40 calls/second
     1-minute rate = 828782.77 calls/second
     5-minute rate = 805015.55 calls/second
    15-minute rate = 799383.97 calls/second
               min = 0.00 microseconds
               max = 3.08 microseconds
              mean = 0.26 microseconds
            stddev = 0.38 microseconds
            median = 0.00 microseconds
              75% <= 0.51 microseconds
              95% <= 1.03 microseconds
              98% <= 1.03 microseconds
              99% <= 1.54 microseconds
            99.9% <= 3.04 microseconds
queue.enqueue
             count = 50000000
         mean rate = 847293.66 calls/second
     1-minute rate = 828820.87 calls/second
     5-minute rate = 805091.33 calls/second
    15-minute rate = 799468.84 calls/second
               min = 0.00 microseconds
               max = 67.75 microseconds
              mean = 0.86 microseconds
            stddev = 3.38 microseconds
            median = 0.00 microseconds
              75% <= 0.51 microseconds
              95% <= 3.59 microseconds
              98% <= 5.13 microseconds
              99% <= 9.24 microseconds
            99.9% <= 67.69 microseconds


Queue: 58,982ms
12/26/13 3:58:45 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 50000000
         mean rate = 1427955.83 calls/second
     1-minute rate = 1413947.31 calls/second
     5-minute rate = 1405293.22 calls/second
    15-minute rate = 1403441.97 calls/second
               min = 12.83 microseconds
               max = 1544.38 microseconds
              mean = 381.21 microseconds
            stddev = 189.88 microseconds
            median = 417.28 microseconds
              75% <= 498.76 microseconds
              95% <= 665.74 microseconds
              98% <= 705.91 microseconds
              99% <= 740.68 microseconds
            99.9% <= 1540.29 microseconds
ring.consume
             count = 50000000
         mean rate = 1427816.92 calls/second
     1-minute rate = 1413947.14 calls/second
     5-minute rate = 1405293.18 calls/second
    15-minute rate = 1403441.95 calls/second
               min = 0.00 microseconds
               max = 0.51 microseconds
              mean = 0.01 microseconds
            stddev = 0.08 microseconds
            median = 0.00 microseconds
              75% <= 0.00 microseconds
              95% <= 0.00 microseconds
              98% <= 0.51 microseconds
              99% <= 0.51 microseconds
            99.9% <= 0.51 microseconds
ring.enqueue
             count = 50000000
         mean rate = 1427668.48 calls/second
     1-minute rate = 1414034.86 calls/second
     5-minute rate = 1405437.06 calls/second
    15-minute rate = 1403597.33 calls/second
               min = 0.00 microseconds
               max = 1.54 microseconds
              mean = 0.08 microseconds
            stddev = 0.19 microseconds
            median = 0.00 microseconds
              75% <= 0.00 microseconds
              95% <= 0.51 microseconds
              98% <= 0.51 microseconds
              99% <= 0.51 microseconds
            99.9% <= 1.51 microseconds


Ring:  34,995ms

### Multi Thread (8 Threads, 1 per Core)
50M messages
12/26/13 4:02:18 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 50000000
         mean rate = 234531.52 calls/second
     1-minute rate = 260847.07 calls/second
     5-minute rate = 189572.14 calls/second
    15-minute rate = 156687.78 calls/second
               min = 1.54 microseconds
               max = 161.16 microseconds
              mean = 16.53 microseconds
            stddev = 16.46 microseconds
            median = 10.78 microseconds
              75% <= 16.94 microseconds
              95% <= 50.86 microseconds
              98% <= 65.18 microseconds
              99% <= 72.95 microseconds
            99.9% <= 160.39 microseconds
queue.consume
             count = 50000000
         mean rate = 234527.34 calls/second
     1-minute rate = 260847.07 calls/second
     5-minute rate = 189572.03 calls/second
    15-minute rate = 156687.62 calls/second
               min = 0.00 microseconds
               max = 0.51 microseconds
              mean = 0.01 microseconds
            stddev = 0.09 microseconds
            median = 0.00 microseconds
              75% <= 0.00 microseconds
              95% <= 0.00 microseconds
              98% <= 0.51 microseconds
              99% <= 0.51 microseconds
            99.9% <= 0.51 microseconds
queue.dequeue
             count = 50000008
         mean rate = 234522.99 calls/second
     1-minute rate = 260846.99 calls/second
     5-minute rate = 189571.92 calls/second
    15-minute rate = 156687.46 calls/second
               min = 0.00 microseconds
               max = 1029.08 microseconds
              mean = 26.34 microseconds
            stddev = 88.54 microseconds
            median = 0.51 microseconds
              75% <= 26.69 microseconds
              95% <= 124.21 microseconds
              98% <= 305.33 microseconds
              99% <= 496.14 microseconds
            99.9% <= 1027.60 microseconds
queue.enqueue
             count = 50000000
         mean rate = 234518.58 calls/second
     1-minute rate = 260847.01 calls/second
     5-minute rate = 189571.81 calls/second
    15-minute rate = 156687.30 calls/second
               min = 0.00 microseconds
               max = 116.00 microseconds
              mean = 2.90 microseconds
            stddev = 10.14 microseconds
            median = 0.00 microseconds
              75% <= 0.51 microseconds
              95% <= 32.34 microseconds
              98% <= 36.44 microseconds
              99% <= 42.45 microseconds
            99.9% <= 114.57 microseconds


Queue: 213,198ms
12/26/13 4:03:39 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 50000000
         mean rate = 622823.31 calls/second
     1-minute rate = 622352.59 calls/second
     5-minute rate = 636949.40 calls/second
    15-minute rate = 640109.04 calls/second
               min = 1.03 microseconds
               max = 111.89 microseconds
              mean = 15.80 microseconds
            stddev = 16.57 microseconds
            median = 8.73 microseconds
              75% <= 21.04 microseconds
              95% <= 46.99 microseconds
              98% <= 62.32 microseconds
              99% <= 89.42 microseconds
            99.9% <= 111.83 microseconds
ring.consume
             count = 50000000
         mean rate = 622795.19 calls/second
     1-minute rate = 622347.88 calls/second
     5-minute rate = 636931.18 calls/second
    15-minute rate = 640083.24 calls/second
               min = 0.00 microseconds
               max = 0.51 microseconds
              mean = 0.03 microseconds
            stddev = 0.12 microseconds
            median = 0.00 microseconds
              75% <= 0.00 microseconds
              95% <= 0.51 microseconds
              98% <= 0.51 microseconds
              99% <= 0.51 microseconds
            99.9% <= 0.51 microseconds
ring.enqueue
             count = 50000000
         mean rate = 622771.44 calls/second
     1-minute rate = 622348.03 calls/second
     5-minute rate = 636931.18 calls/second
    15-minute rate = 640083.25 calls/second
               min = 0.00 microseconds
               max = 87.25 microseconds
              mean = 1.26 microseconds
            stddev = 6.84 microseconds
            median = 0.00 microseconds
              75% <= 0.51 microseconds
              95% <= 0.51 microseconds
              98% <= 31.01 microseconds
              99% <= 36.81 microseconds
            99.9% <= 86.99 microseconds


Ring:  80,280ms

## Print Producer

### Single Thread

10M messages
12/26/13 4:04:07 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 10000000
         mean rate = 352122.34 calls/second
     1-minute rate = 339102.40 calls/second
     5-minute rate = 333905.41 calls/second
    15-minute rate = 332894.03 calls/second
               min = 2156.19 microseconds
               max = 11586.73 microseconds
              mean = 2881.99 microseconds
            stddev = 443.24 microseconds
            median = 2853.70 microseconds
              75% <= 3036.42 microseconds
              95% <= 3366.26 microseconds
              98% <= 3974.72 microseconds
              99% <= 4262.95 microseconds
            99.9% <= 11392.15 microseconds
queue.consume
             count = 10000000
         mean rate = 352098.87 calls/second
     1-minute rate = 339101.76 calls/second
     5-minute rate = 333904.49 calls/second
    15-minute rate = 332893.06 calls/second
               min = 0.51 microseconds
               max = 59.54 microseconds
              mean = 1.36 microseconds
            stddev = 3.22 microseconds
            median = 1.03 microseconds
              75% <= 1.03 microseconds
              95% <= 1.54 microseconds
              98% <= 4.54 microseconds
              99% <= 15.25 microseconds
            99.9% <= 59.43 microseconds
queue.dequeue
             count = 10000001
         mean rate = 352076.20 calls/second
     1-minute rate = 339101.46 calls/second
     5-minute rate = 333904.11 calls/second
    15-minute rate = 332892.67 calls/second
               min = 0.00 microseconds
               max = 4.62 microseconds
              mean = 0.45 microseconds
            stddev = 0.50 microseconds
            median = 0.51 microseconds
              75% <= 1.03 microseconds
              95% <= 1.03 microseconds
              98% <= 1.03 microseconds
              99% <= 1.90 microseconds
            99.9% <= 4.61 microseconds
queue.enqueue
             count = 10000000
         mean rate = 352053.21 calls/second
     1-minute rate = 339247.05 calls/second
     5-minute rate = 334094.20 calls/second
    15-minute rate = 333091.40 calls/second
               min = 0.00 microseconds
               max = 96.49 microseconds
              mean = 2.22 microseconds
            stddev = 4.83 microseconds
            median = 0.51 microseconds
              75% <= 3.59 microseconds
              95% <= 7.19 microseconds
              98% <= 11.81 microseconds
              99% <= 18.48 microseconds
            99.9% <= 95.57 microseconds


Queue: 28,399ms
12/26/13 4:04:25 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 10000000
         mean rate = 558029.63 calls/second
     1-minute rate = 547802.96 calls/second
     5-minute rate = 545572.86 calls/second
    15-minute rate = 545171.96 calls/second
               min = 806.84 microseconds
               max = 3481.92 microseconds
              mean = 1430.52 microseconds
            stddev = 291.35 microseconds
            median = 1430.18 microseconds
              75% <= 1587.24 microseconds
              95% <= 1908.33 microseconds
              98% <= 2099.14 microseconds
              99% <= 2317.02 microseconds
            99.9% <= 3466.01 microseconds
ring.consume
             count = 10000000
         mean rate = 557956.71 calls/second
     1-minute rate = 547802.63 calls/second
     5-minute rate = 545572.48 calls/second
    15-minute rate = 545171.56 calls/second
               min = 0.51 microseconds
               max = 100.08 microseconds
              mean = 1.19 microseconds
            stddev = 3.51 microseconds
            median = 1.03 microseconds
              75% <= 1.03 microseconds
              95% <= 1.54 microseconds
              98% <= 3.00 microseconds
              99% <= 13.34 microseconds
            99.9% <= 97.76 microseconds
ring.enqueue
             count = 10000000
         mean rate = 557894.88 calls/second
     1-minute rate = 547960.75 calls/second
     5-minute rate = 545753.31 calls/second
    15-minute rate = 545356.48 calls/second
               min = 0.00 microseconds
               max = 729.34 microseconds
              mean = 1.45 microseconds
            stddev = 30.83 microseconds
            median = 0.00 microseconds
              75% <= 0.00 microseconds
              95% <= 0.51 microseconds
              98% <= 0.51 microseconds
              99% <= 0.51 microseconds
            99.9% <= 727.56 microseconds


Ring:  17,917ms

### Multi Thread (8 Threads, 1 per Core)
10M messages
12/26/13 4:04:49 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 10000000
         mean rate = 414477.70 calls/second
     1-minute rate = 413914.76 calls/second
     5-minute rate = 413595.85 calls/second
    15-minute rate = 413536.43 calls/second
               min = 23.10 microseconds
               max = 13402.12 microseconds
              mean = 2496.92 microseconds
            stddev = 419.72 microseconds
            median = 2432.83 microseconds
              75% <= 2498.01 microseconds
              95% <= 2754.90 microseconds
              98% <= 3564.82 microseconds
              99% <= 3693.36 microseconds
            99.9% <= 13142.58 microseconds
queue.consume
             count = 10000000
         mean rate = 414434.95 calls/second
     1-minute rate = 413913.86 calls/second
     5-minute rate = 413594.71 calls/second
    15-minute rate = 413535.26 calls/second
               min = 1.03 microseconds
               max = 380.84 microseconds
              mean = 16.96 microseconds
            stddev = 46.85 microseconds
            median = 3.08 microseconds
              75% <= 5.13 microseconds
              95% <= 126.03 microseconds
              98% <= 204.41 microseconds
              99% <= 245.74 microseconds
            99.9% <= 378.77 microseconds
queue.dequeue
             count = 10000008
         mean rate = 414393.15 calls/second
     1-minute rate = 413914.41 calls/second
     5-minute rate = 413595.49 calls/second
    15-minute rate = 413536.05 calls/second
               min = 0.00 microseconds
               max = 15.40 microseconds
              mean = 0.75 microseconds
            stddev = 1.32 microseconds
            median = 0.51 microseconds
              75% <= 1.03 microseconds
              95% <= 2.05 microseconds
              98% <= 3.59 microseconds
              99% <= 8.58 microseconds
            99.9% <= 15.26 microseconds
queue.enqueue
             count = 10000000
         mean rate = 414352.78 calls/second
     1-minute rate = 414072.64 calls/second
     5-minute rate = 413788.59 calls/second
    15-minute rate = 413735.69 calls/second
               min = 0.00 microseconds
               max = 1183.05 microseconds
              mean = 3.06 microseconds
            stddev = 37.15 microseconds
            median = 0.00 microseconds
              75% <= 2.05 microseconds
              95% <= 7.19 microseconds
              98% <= 19.04 microseconds
              99% <= 26.03 microseconds
            99.9% <= 1151.17 microseconds


Queue: 24,125ms
12/26/13 4:05:08 PM ============================================================

-- Timers ----------------------------------------------------------------------
event.consume
             count = 10000000
         mean rate = 540661.60 calls/second
     1-minute rate = 541490.02 calls/second
     5-minute rate = 541569.13 calls/second
    15-minute rate = 541583.36 calls/second
               min = 400.85 microseconds
               max = 2821.36 microseconds
              mean = 1144.92 microseconds
            stddev = 292.67 microseconds
            median = 1152.77 microseconds
              75% <= 1320.99 microseconds
              95% <= 1558.52 microseconds
              98% <= 1717.97 microseconds
              99% <= 2182.51 microseconds
            99.9% <= 2818.77 microseconds
ring.consume
             count = 10000000
         mean rate = 540589.17 calls/second
     1-minute rate = 541489.42 calls/second
     5-minute rate = 541568.56 calls/second
    15-minute rate = 541582.77 calls/second
               min = 1.03 microseconds
               max = 590.24 microseconds
              mean = 12.43 microseconds
            stddev = 57.97 microseconds
            median = 2.57 microseconds
              75% <= 3.08 microseconds
              95% <= 12.14 microseconds
              98% <= 263.27 microseconds
              99% <= 377.38 microseconds
            99.9% <= 590.23 microseconds
ring.enqueue
             count = 10000000
         mean rate = 540519.57 calls/second
     1-minute rate = 541629.25 calls/second
     5-minute rate = 541729.39 calls/second
    15-minute rate = 541747.43 calls/second
               min = 0.00 microseconds
               max = 745.76 microseconds
              mean = 1.49 microseconds
            stddev = 31.82 microseconds
            median = 0.00 microseconds
              75% <= 0.00 microseconds
              95% <= 0.51 microseconds
              98% <= 0.51 microseconds
              99% <= 0.51 microseconds
            99.9% <= 744.35 microseconds


Ring:  18,494ms