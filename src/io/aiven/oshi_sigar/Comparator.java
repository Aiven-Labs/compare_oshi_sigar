package io.aiven.oshi_sigar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Comparator {

    // TODO: Determine memlock limits if possible
    // TODO: Determine if file system is remote or local
    // TODO: Determine if disk latency is within acceptable limits

    private static Logger logger = LoggerFactory.getLogger(Comparator.class);

    public static boolean hasAcceptableProcNumber(CommonFuncs funcs) {
        try {
            return Expected.PROC_P.test(funcs.getMaxProcess());
        } catch (Exception e) {
            logger.warn("Could not determine if max processes was acceptable. Error message: {}", e);
            return false;
        }
    }

    public static boolean hasAcceptableFileLimits(CommonFuncs funcs) {
        try {
            return Expected.FILE_P.test(funcs.getMaxOpenFiles());
        } catch (Exception e) {
            logger.warn("Could not determine if max open file handle limit is correctly configured. Error message: {}",
                    e);
            return false;
        }
    }

    public static boolean hasAcceptableAddressSpace(CommonFuncs funcs) {
        try {
            return Expected.MEM_P.test(funcs.getVirtualMemoryMax());
        } catch (Exception e) {
            logger.warn("Could not determine if VirtualMemoryMax was acceptable. Error message: {}", e);
            return false;
        }
    }

    public static boolean isSwapEnabled(CommonFuncs funcs) {
        try {
            return funcs.getSwapSize() > 0;
        } catch (Exception e) {
            logger.warn("Could not determine if swap configuration is acceptable. Error message: {}", e);
            return false;
        }
    }

    public static void warnIfRunningInDegradedMode(CommonFuncs funcs) {
        boolean swapEnabled = isSwapEnabled(funcs);
        boolean goodAddressSpace = hasAcceptableAddressSpace(funcs);
        boolean goodFileLimits = hasAcceptableFileLimits(funcs);
        boolean goodProcNumber = hasAcceptableProcNumber(funcs);
        if (swapEnabled || !goodAddressSpace || !goodFileLimits || !goodProcNumber) {
            logger.warn(
                    "Cassandra server running in degraded mode. Is swap disabled? : {},  Address space adequate? : {}, "
                            + " nofile limit adequate? : {}, nproc limit adequate? : {} ",
                    !swapEnabled, goodAddressSpace, goodFileLimits, goodProcNumber);
        } else {
            logger.info("Checked OS settings and found them configured for optimal performance.");
        }

    }

    private static final int SWAP=0;
    private static final int MEM=1;
    private static final int FILE=2;
    private static final int PROC=3;
    
    public static boolean[] report(CommonFuncs funcs) throws Exception {
        long swap = funcs.getSwapSize();
        long virtMem = funcs.getVirtualMemoryMax();
        long fileMax = funcs.getMaxOpenFiles();
        long procMax = funcs.getMaxProcess();
        boolean[] result = new boolean[4];
        result[SWAP] = !isSwapEnabled(funcs);
        result[MEM] = hasAcceptableAddressSpace(funcs);
        result[FILE] = hasAcceptableFileLimits(funcs);
        result[PROC] = hasAcceptableProcNumber(funcs);

        System.out.format("%10s swap: %s/%s\tvirtmem: %s/%s\tfiles: %s/%s\tproc: %s/%s\tpid: %s\tstatus: %s\n",
                funcs.name(), swap, result[SWAP], virtMem, result[MEM], fileMax,
                result[FILE], procMax, result[PROC], funcs.getPid(),
                (result[SWAP] & result[MEM] & result[FILE] & result[PROC]));
        return result;
    }

    
    public static void main(String[] args) throws Exception {
        boolean[] sigar = report(new SigarLibrary());
        boolean[] oshi = report(new OshiLibrary());
        boolean result = true;
        for (int i=0;i<sigar.length;i++) {
            result = result & (sigar[i]==oshi[i]);
        }
        System.out.println( "results are"+(result?" ":" not ")+"equal");
    }
}
