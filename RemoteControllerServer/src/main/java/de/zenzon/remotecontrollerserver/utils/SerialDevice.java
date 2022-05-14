package de.zenzon.remotecontrollerserver.utils;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class SerialDevice implements Closeable {

    SerialPort serialPort;
    int baud;

    public SerialDevice(String portname, int baud) {
        this(new SerialPort(portname), baud);
    }

    public SerialDevice(SerialPort serialPort, int baud) {
        this.serialPort = serialPort;
        this.baud = baud;
        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
        if (!serialPort.isOpened())
            open();
    }

    public void open() {
        try {
            serialPort.openPort();
            serialPort.setParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, true, false);

        } catch (Error | SerialPortException f) {
            f.printStackTrace();
        }
    }

    public String sendCommandAndWaitForOutput(String command) throws SerialPortException {
        serialPort.writeString(command);
        String s;
        while ((s = read(5000)) == null) {
            System.out.println("timeout");
            serialPort.writeString(command);
        }
        return s;
    }

    public String read() throws SerialPortException {
        return read(-1);
    }


    public String read(int timeout) throws SerialPortException {
        long started = timeout != -1 ? System.currentTimeMillis() + timeout : -1;
        byte[] buffer = new byte[4096];
        int bufferPos = 0;
        while (true) {
            if (System.currentTimeMillis() > started && started != -1)
                return null;
            byte[] data = serialPort.readBytes();
            if (data != null) {
                for (int i = 0; i < data.length; ++i) {
                    if (bufferPos > 4095) {
                        System.out.println(new String(buffer, 0, bufferPos - 1));
                        bufferPos = 0;
                    }
                    buffer[bufferPos++] = data[i];
                }
                if (data.length > 0) {
                    if (buffer[bufferPos - 1] == '\n') {
                        if (bufferPos == 1) {
                            return read();
                        }
                        return new String(buffer, 0, bufferPos - 1);
                    }
                }
            }
        }
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public static String[] getSerialPorts() {
        return SerialPortList.getPortNames();
    }

    @Override
    public void close() throws IOException {
        try {
            serialPort.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }
}