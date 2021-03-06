package org.kevoree.watchdog.child.watchdog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by duke on 17/05/13.
 */
public class WatchdogClient implements Runnable {

    private Integer internalPort = -1;

    public WatchdogClient(Integer _internalPort) {
        internalPort = _internalPort;
    }

    @Override
    public void run() {
        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] sendData = "alive".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, internalPort);
            clientSocket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
