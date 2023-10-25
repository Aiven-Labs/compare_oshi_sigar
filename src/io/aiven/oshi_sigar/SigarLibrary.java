package io.aiven.oshi_sigar;

import org.hyperic.sigar.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SigarLibrary implements CommonFuncs
{
    private Logger logger = LoggerFactory.getLogger(SigarLibrary.class);

    private Sigar sigar;
    private boolean initialized = false;


    public SigarLibrary()
    {
        logger.info("Initializing SIGAR library");
        try
        {
            sigar = new Sigar();
            sigar.getFileSystemMap();
            initialized = true;
        }
        catch (SigarException e)
        {
            logger.info("Could not initialize SIGAR library {} ", e.getMessage());
        }
        catch (UnsatisfiedLinkError linkError)
        {
            logger.info("Could not initialize SIGAR library {} ", linkError.getMessage());
        }
    }

    /**
     *
     * @return true or false indicating if sigar was successfully initialized
     */
    public boolean initialized()
    {
        return initialized;
    }

    @Override
    public long getMaxProcess() throws SigarException {
        // ulimit -u
        return sigar.getResourceLimit().getProcessesMax();
    }

    
    @Override
    public long getMaxOpenFiles() throws SigarException {
        // ulimit -H -n
        return sigar.getResourceLimit().getOpenFilesMax();
    }

    @Override
    public long getSwapSize() throws SigarException {
        return sigar.getSwap().getTotal();
    }

    public long getVirtualMemoryMax() throws SigarException {
        ResourceLimit rl = sigar.getResourceLimit();
        return  rl.getVirtualMemoryMax();
    }
    
    public long getPid()
    {
        return initialized ? sigar.getPid() : -1;
    }
    

    @Override
    public String name() {
        return "Sigar";
    }


    public static void main(String[] args) throws Exception {
        Comparator.report(new SigarLibrary() );
    }

   
}
