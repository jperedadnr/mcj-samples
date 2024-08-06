module org.modernclients.raspberrypi.gps {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.pi4j;
    requires com.gluonhq.maps;
    requires afterburner.mfx;
    requires java.annotation;
    requires java.logging;

    opens org.modernclients.raspberrypi.gps.model to afterburner.mfx;
    opens org.modernclients.raspberrypi.gps.service to afterburner.mfx;
    opens org.modernclients.raspberrypi.gps.view to afterburner.mfx, javafx.fxml;

    exports org.modernclients.raspberrypi.gps;
}