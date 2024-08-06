package org.modernclients.raspberrypi.gps.service;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.StopBits;
import com.pi4j.util.Console;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.modernclients.raspberrypi.gps.model.GPSPosition;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class GPSService {

    private static final Logger logger = Logger.getLogger(GPSService.class.getName());

    @Inject
    private GPSPosition gpsPosition;

    private Serial serial;
    private NMEAParser nmea;
    private StringBuilder gpsOutput;

    private final StringProperty line = new SimpleStringProperty();

    @PostConstruct
    private void postConstruct() {
        if (! "monocle".equals(System.getProperty("embedded"))) {
            return;
        }

        nmea = new NMEAParser(gpsPosition);
        gpsOutput = new StringBuilder();

        Console console = new Console();
        Context context = Pi4J.newAutoContext();

        serial = context.create(Serial.newConfigBuilder(context)
                .use_9600_N81()
                .dataBits_8()
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE)
                .id("my-serial")
                .device("/dev/ttyAMA0")
                .build());

        try {
            // Wait till the serial port is open
            console.print("Waiting till serial port is open");
            while (!serial.isOpen()) {
                Thread.sleep(250);
            }

            // Start a thread to handle the incoming data from the serial port
            SerialReader serialReader = new SerialReader(console, serial);
            Thread serialReaderThread = new Thread(serialReader, "SerialReader");
            serialReaderThread.setDaemon(true);
            serialReaderThread.start();

            while (serial.isOpen()) {
                Thread.sleep(500);
            }

            serialReader.stopReading();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class SerialReader implements Runnable {

        private final Console console;
        private final Serial serial;

        private boolean continueReading = true;

        public SerialReader(Console console, Serial serial) {
            this.console = console;
            this.serial = serial;
        }

        public void stopReading() {
            continueReading = false;
        }

        @Override
        public void run() {
            // We use a buffered reader to handle the data received from the serial port
            try (BufferedReader br = new BufferedReader(new InputStreamReader(serial.getInputStream()))) {
                try {
                    // Data from the GPS is received in lines
                    String line = "";

                    // Read data until the flag is false
                    while (continueReading) {
                        // First we need to check if there is data available to read.
                        // The read() command for pigio-serial is a NON-BLOCKING call,
                        // in contrast to typical java input streams.
                        var available = serial.available();
                        if (available > 0) {
                            for (int i = 0; i < available; i++) {
                                byte b = (byte) br.read();
                                if (b < 32) {
                                    // All non-string bytes are handled as line breaks
                                    if (!line.isEmpty()) {
                                        // Here we should add code to parse the data to a GPS data object
                                        console.println("Data: '" + line + "'");
                                        gpsOutput.append(line);
                                        line = "";
                                        processReading();
                                    }
                                } else {
                                    line += (char) b;
                                }
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    }
                } catch (Exception e) {
                    console.println("Error reading data from serial: " + e.getMessage());
                    e.getStackTrace();
                }
            } catch (IOException e) {
                console.println("Error reading data from serial: " + e.getMessage());
                e.getStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private void processReading() {
        if (gpsOutput == null || gpsOutput.toString().isEmpty()) {
            return;
        }
        
        String reading = gpsOutput.toString().trim();

        if (!reading.contains("$")) {
            return;
        }
        
        String[] split = reading.split("\\$");
        
        for (int i = 0; i < split.length - 1; i++) {
            String line = "$" + split[i];
            gpsOutput.delete(0 , line.length());
            if (line.length() > 1) {
                logger.fine("GPS: " + line);
                Platform.runLater(() -> {
                    nmea.parse(line);
                    this.line.set(line);
                });
            }
            if (i == split.length - 2) {
                gpsOutput.insert(0, "$");
            }
        }
    }

    public final StringProperty lineProperty() {
        return line;
    }

    public void stop() {
        logger.info("Stopping Serial and GPIO");
        if (serial != null) {
            try {
                serial.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
