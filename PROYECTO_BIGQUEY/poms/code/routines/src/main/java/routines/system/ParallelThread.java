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

/**
 * DOC stan_2 class global comment. Detailled comment
 */
public class ParallelThread extends Thread {

    protected Integer errorCode = null;

    protected String status = ""; //$NON-NLS-1$

    protected ThreadedMap globalMap = null;

    // the working status of ParallelThread
    private volatile boolean isFree = false;

    public synchronized void setFree(boolean isFree) {
        this.isFree = isFree;
        this.notify();
    }

    public synchronized boolean isFree() {
        return this.isFree;
    }

    public synchronized void waitForFree() throws InterruptedException {
        while (!this.isFree()) {
            wait();
        }
    }

    // A flag for pool stop the ParallelThread
    private volatile boolean isFinish = false;

    /**
     *
     *
     * @return
     */
    public synchronized boolean isFinish() {
        return this.isFinish;
    }

    public synchronized void finish() {
        this.isFinish = true;
        this.notifyAll();
    }

    // buffer the working source
    private Object buffer = null;

    private synchronized void waitForEmpty() throws InterruptedException {
        while (buffer != null) {
            wait();
        }
    }

    private synchronized void waitForNotEmpty() throws InterruptedException {
        while (buffer == null) {
            wait();
        }
    }

    private synchronized Object get() throws InterruptedException {
        waitForNotEmpty();
        return this.buffer;
    }

    private synchronized void set(Object buffer) throws InterruptedException {
        waitForEmpty();
        this.buffer = buffer;
        notifyAll();
    }

    private synchronized void clear() {
        this.buffer = null;
        notifyAll();
    }

    public synchronized void clearBuffer() {
        clear();
    }

    /**
     * Waiting for buffer is not null get and remove the buffer. Be called by ParallelThread.
     *
     * @return
     * @throws InterruptedException
     */
    public synchronized Object pollBuffer() throws InterruptedException {
        Object buf = this.get();
        this.clear();
        this.notifyAll();
        return buf;
    }

    /**
     * Waiting for buffer is null and set buffer if null Be called by main thread.
     *
     * @param buffer
     * @throws InterruptedException
     */
    public synchronized void putBuffer(Object buffer) throws InterruptedException {
        this.set(buffer);
    }

    // the id of thread for TOS
    private int threadID = 0;

    public int getThreadID() {
        return this.threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public ParallelThread(java.util.Map<String, Object> globalMap, Object[] lockWrite) {
        super();
        this.globalMap = new ThreadedMap(globalMap);
        this.globalMap.myMap.put("PARALLEL_LOCK_WRITE", lockWrite); //$NON-NLS-1$
    }

    protected class ThreadedMap extends java.util.HashMap<String, Object> {

        java.util.Map<String, Object> myMap = new java.util.HashMap<String, Object>();

        private static final long serialVersionUID = 0L;

        public ThreadedMap(java.util.Map<String, Object> globalMap) {
            synchronized (globalMap) {
                for (java.util.Map.Entry<String, Object> entry : globalMap.entrySet()) {
                    myMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        @Override
        public Object get(Object key) {
            return myMap.get(key);
        }

        @Override
        public Object put(String key, Object value) {
            return myMap.put(key, value);
            // return ParallelDB.globalMap.put(key, value);
        }
    }
}
