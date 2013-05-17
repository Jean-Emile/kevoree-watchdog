package org.kevoree.boot;

import org.kevoree.boot.child.jvm.ChildJVM;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by duke on 17/05/13.
 */
public class WatchDogCheck implements Runnable {

    public static final Integer internalPort = 9999;
    public static final Integer checkTime = 3000;

    public void setRuntimeFile(File runtimeFile) {
        this.runtimeFile = runtimeFile;
    }

    private File runtimeFile = null;

    public void setModelFile(File modelFile) {
        this.modelFile = modelFile;
    }

    private File modelFile = null;

    private AtomicLong lastCheck = new AtomicLong();
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    private Process currentProcess = null;

    @Override
    public void run() {
        Long dif = System.currentTimeMillis() - lastCheck.get();
        if (dif > checkTime) {
            System.err.println("Kevoree Runtime does not send news since " + checkTime + " force restart");
            currentProcess.destroy();
            startKevoreeProcess();
        }
    }

    private static Thread serverThread = null;

    public void startServer() {
        serverThread = new Thread(new WatchDogServer());
        serverThread.setDaemon(true);
        serverThread.start();
        lastCheck.set(System.currentTimeMillis());
        pool.scheduleAtFixedRate(this, WatchDogCheck.checkTime, WatchDogCheck.checkTime, TimeUnit.MILLISECONDS);
    }

    public void startKevoreeProcess() {
        ArrayList<String> childargs = new ArrayList<String>();
        ArrayList<String> childmainargs = new ArrayList<String>();
        childargs.add("-Dkevruntime=" + runtimeFile.getAbsolutePath());
        if (modelFile != null) {
            childargs.add("-Dnode.bootstrap=" + modelFile.getAbsolutePath());
        }
        if(System.getProperty("node.name")!=null && System.getProperty("node.name") != ""){
            childargs.add("-Dnode.name=" + System.getProperty("node.name"));
        }
        currentProcess = new ChildJVM.Builder()
                .withMainClassName("org.kevoree.boot.child.watchdog.ChildRunner")
                .withAdditionalCommandLineArguments(childargs)
                .withMainClassArguments(childmainargs)
                .withInheritClassPath(true).isolate();
    }


    private class WatchDogServer implements Runnable {
        @Override
        public void run() {
            DatagramSocket serverSocket = null;
            try {
                serverSocket = new DatagramSocket(internalPort);
                byte[] receiveData = new byte[1024];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    lastCheck.set(System.currentTimeMillis());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
