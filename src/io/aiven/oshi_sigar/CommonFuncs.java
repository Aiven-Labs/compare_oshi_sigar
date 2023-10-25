package io.aiven.oshi_sigar;


public interface CommonFuncs {
    String name();
   long getMaxProcess() throws Exception;
   long getMaxOpenFiles() throws Exception;
   long getVirtualMemoryMax() throws Exception;
   long getSwapSize() throws Exception;
   long getPid();
}
