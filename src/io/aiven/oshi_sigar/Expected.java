package io.aiven.oshi_sigar;

import java.util.function.LongPredicate;

public interface Expected {

    long INFINITY = -1;
    long EXPECTED_MIN_NOFILE = 10000l; // number of files that can be opened
    long EXPECTED_NPROC = 32768l; // number of processes
    long EXPECTED_AS = 0x7FFFFFFFl; // address space
    long DEFAULT_MAX_PROCESSES = 1024;
    
    LongPredicate MEM_P = (x) -> (x >= Expected.EXPECTED_AS || x == Expected.INFINITY);
    LongPredicate FILE_P = (x) -> (x >= Expected.EXPECTED_MIN_NOFILE || x == Expected.INFINITY);
    LongPredicate PROC_P = (x) -> (x >= Expected.EXPECTED_NPROC || x == Expected.INFINITY);

}
