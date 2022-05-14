package de.zenzon.remotecontrollerserver.main;

import de.zenzon.remotecontrollerserver.utils.SerialDevice;
import de.zenzon.remotecontrollerserver.utils.Translate;
import jssc.SerialPortException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : ZenZon
 * @emailto : florian@einerhand.net
 * @since : 13.05.2022, Fr.
 **/
public class Main {

    public static Translate translate;

    public static void main(String[] args) throws SerialPortException {

        if (args.length < 1) {
            System.out.println("Please enter at least a serial port.\njava -jar RemoteControllerServer.jar <serial> [networkPort]");
            return;
        }

        int port = 4445;

        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        SerialDevice serialDevice = new SerialDevice(args[0], 9600) {

        };

        translate = new Translate();

        try {
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buf = new byte[256];
            AtomicReference<Thread> doubleStart = new AtomicReference<>(null);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.startsWith("se")) {
                    serialDevice.getSerialPort().writeBytes(translate.getState());
                }else
                if (received.startsWith("bs")) {
                    // some filtering for the start button to press home when it is pressed for over 2 seconds
                    if (received.startsWith("bs1")) {
                        doubleStart.set(new Thread(() -> {
                            long start = System.currentTimeMillis();
                            try {
                                while (doubleStart.get() != null && System.currentTimeMillis() - start < 2000) {
                                    Thread.sleep(10);
                                }

                                if (doubleStart.get() == null)
                                    return;
                                if (Thread.interrupted())
                                    return;

                                doubleStart.set(null);
                                sendOut("bh1");
                                try {
                                    serialDevice.getSerialPort().writeBytes(translate.getState());
                                } catch (SerialPortException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                sendOut("bh0");
                                try {
                                    serialDevice.getSerialPort().writeBytes(translate.getState());
                                } catch (SerialPortException e) {
                                    e.printStackTrace();
                                }
                            } catch (InterruptedException ignored) {
                            }
                        }));
                        doubleStart.get().start();
                    }
                    if (received.startsWith("bs0")) {
                        if (doubleStart.get() != null) {
                            doubleStart.get().interrupt();
                            doubleStart.set(null);
                            new Thread(() -> {
                                sendOut("bs1");
                                try {
                                    serialDevice.getSerialPort().writeBytes(translate.getState());
                                } catch (SerialPortException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                sendOut("bs0");
                                try {
                                    serialDevice.getSerialPort().writeBytes(translate.getState());
                                } catch (SerialPortException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                    }
                } else {
                    sendOut(received);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendOut(String input) {
        translate.input(input);
    }

}
