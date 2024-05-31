// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package routines.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DOC xzhang class global comment. Detailled comment
 */
public class ParallelThreadPool {

    private volatile boolean stopAllWorkers = false;

    private ParallelThread errorThread = null;

    private TalendThreadResult threadResult = null;

    private ParallelThread[] threads;

    private int poolMaxSize = 0;

    private int poolCurrentSize = 0;

    /**
     * init the thread list
     *
     * @param poolSize the size of pool
     */
    public ParallelThreadPool(int poolSize) {
        this.threadResult = new TalendThreadResult();
        this.poolMaxSize = Math.max(1, poolSize);
        this.threads = new ParallelThread[this.poolMaxSize];
    }

    /**
     * add and run the thread
     *
     * @param pt
     */
    public void execThread(ParallelThread pt) throws Exception {
        checkException();

        if (!stopAllWorkers) {
            pt.setThreadID(poolCurrentSize);
            this.threads[poolCurrentSize++] = pt;
            pt.start();
        }
    }

    public boolean isFull() {
        return this.poolCurrentSize >= this.poolMaxSize;
    }

    /**
     * get free thread for setting new rows
     *
     * @return
     */

    public ParallelThread getFreeThread() throws Exception {
        checkException();

        while (!stopAllWorkers) {
            for (ParallelThread tmp : this.threads) {
                if (tmp != null && tmp.isFree()) {
                    return tmp;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // ExceptionHandler.process(e);
            }
        }
        return null;
    }

    /**
     *
     *
     * DOC stan_zhang Comment method "waitForEnd".
     */
    public void waitForEnd() throws Exception {
        try {
            if (!stopAllWorkers) {
                for (ParallelThread tmp : this.threads) {
                    // if there's little rows, threads isn't full, tmp will be null.
                    if (tmp != null) {
                        // make sure the parallel thread is waiting for buffer
                        tmp.waitForFree();
                        // set finish
                        tmp.finish();
                        // insert a empty buffer to break the waiting buffer
                        tmp.putBuffer(new ArrayList<String[]>());
                    }

                    checkException();
                }
            } else {
                checkException();
            }
            while (!stopAllWorkers) {
                boolean hasThreadWork = false;
                for (ParallelThread tmp : this.threads) {
                    if (tmp != null && tmp.isAlive()) {
                        hasThreadWork = true;
                    }
                }
                if (hasThreadWork) {
                    Thread.sleep(100);
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // ExceptionHandler.process(e);
        }
    }

    /**
     * This method called after all threads are done and collect "_NB_LINES" values.
     * Need collect data from all threads.
     * Then put to global map if data not exist, because when thread created
     * we clone values globalMap -> thread.globalMap.
     *
     * In order to fix the bug:TDI-28741, TDI-41718
     * @param globalMap - real global map
     */
    public void setGlobalVariables(final java.util.Map globalMap) {

        Map<String, Object> aggregateMap = new HashMap<String, Object>();
        for (ParallelThread tmp : this.threads) {
            if (tmp != null) {
                for (String key : tmp.globalMap.myMap.keySet()) {
                    if (key.contains("_NB_LINE")) {
                        if (tmp.globalMap.get(key) instanceof Number) {
                            int tempValue = ((Number) tmp.globalMap.get(key)).intValue();
                            if (aggregateMap.get(key) == null) {
                                aggregateMap.put(key, tempValue);
                            } else {
                                aggregateMap.put(key, ((Number) aggregateMap.get(key)).intValue() + tempValue);
                            }
                        } else if (!aggregateMap.containsKey(key)) {
                            aggregateMap.put(key, tmp.globalMap.get(key));
                        }
                    }
                }
            }
        }
        //  put aggregated values to global map if it don't have those values
        for (String key : aggregateMap.keySet()){
            if(key.contains("_NB_LINE") && !globalMap.containsKey(key)) {
                globalMap.put(key, aggregateMap.get(key));
            }
        }
    }

    public synchronized void stopAllThreads() {
        if (!stopAllWorkers) {
            try {
                stopAllWorkers = true;
                for (ParallelThread tmp : this.threads) {
                    if (tmp != null) {
                        tmp.interrupt();
                        tmp.clearBuffer();
                        tmp.setFree(true);
                        tmp.finish();
                        while (tmp.isAlive()) {
                            Thread.sleep(100);
                        }
                    }
                }
            } catch (InterruptedException x) {
            }

        }
    }

    /**
     * get ErrorThread.
     *
     * @return
     */
    public ParallelThread getErrorThread() {
        return errorThread;
    }

    /**
     * only keep the first ErrorThread
     *
     * @param errorThread
     */
    public synchronized void setErrorThread(ParallelThread errorThread, Exception exception) {
        if (this.errorThread == null) {
            this.errorThread = errorThread;
        }
        if (getTalendThreadResult().getException() == null) {
            this.getTalendThreadResult().setException(exception);
        }
    }

    public TalendThreadResult getTalendThreadResult() {
        return threadResult;
    }

    private void checkException() throws Exception {
        if (this.getErrorThread() != null) {
            throw this.getTalendThreadResult().getException();
        }
    }
}
