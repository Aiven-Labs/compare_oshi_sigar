package io.aiven.oshi_sigar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;
import oshi.util.ExecutingCommand;

public class OshiLibrary implements CommonFuncs {

    private Logger logger = LoggerFactory.getLogger(OshiLibrary.class);

    private SystemInfo si;

    public OshiLibrary() {
        si = new SystemInfo();
    }

    @Override
    public String name() {
        return "OSHI";
    }

    @Override
    public long getMaxProcess() throws Exception {
        // we want ulimit -u
        try {
            switch (SystemInfo.getCurrentPlatform()) {
            case LINUX:
                return getMaxProcessFromProc();
            case AIX:
                break;
            case ANDROID:
                break;
            case FREEBSD:
                break;
            case GNU:
                break;
            case KFREEBSD:
                break;
            case MACOS:
                return Hacks.getMaxProcess();
            case NETBSD:
                break;
            case OPENBSD:
                break;
            case SOLARIS:
                break;
            case UNKNOWN:
                break;
            case WINDOWS:
                break;
            case WINDOWSCE:
                break;
            default:
                break;
            }
            logger.info("No max processes calculation for OS type: " + SystemInfo.getCurrentPlatform());
        } catch (Exception e) {
            logger.error("can not retrieve max processes", e);
        }
        // We can't figure out the limit so return default.
        return Expected.DEFAULT_MAX_PROCESSES;
    }

    @Override
    public long getMaxOpenFiles() throws Exception {
        // ulimit -H -n
        return si.getOperatingSystem().getCurrentProcess().getHardOpenFileLimit();
    }

    @Override
    public long getVirtualMemoryMax() throws Exception {
        return si.getOperatingSystem().getCurrentProcess().getVirtualSize();
    }

    @Override
    public long getSwapSize() throws Exception {
        GlobalMemory memory = si.getHardware().getMemory();
        VirtualMemory virtualMemory = memory.getVirtualMemory();
        return virtualMemory.getSwapTotal();
    }

    @Override
    public long getPid() {
        return si.getOperatingSystem().getProcessId();
    }

    public static void main(String[] args) throws Exception {
        Comparator.report(new OshiLibrary());
    }

    private long getMaxProcessFromProc() throws IOException {
        Path p = Paths.get("/proc", Long.toString(getPid()), "limits");
        List<String> lines = Files.readAllLines(p);
        for (String s : lines) {
            if (s.startsWith("Max processes")) {
                String[] parts = s.split("\s+");
                String limit = parts[2];
                if ("unlimited".equals(limit)) {
                    return Expected.INFINITY;
                }
                return Long.parseLong(limit);
            }
        }
        throw new IOException("'Max processes' not found in " + p);
    }

    private static class Hacks {
        public static long getMaxProcess() throws Exception {
            List<String> value = ExecutingCommand.runNative(new String[] { "/bin/bash", "-c", "ulimit -u" });
            return Long.parseLong(value.get(0));
        }
    }
}
