package main.java.com.cyFramework.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private final String nomActeur;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Logger(String nomActeur) {
        this.nomActeur = nomActeur;
    }

    private void log(String niveau, String message) {
        String horodatage = LocalDateTime.now().format(FORMATTER);
        System.out.printf("[%s] [%s] [%s] %s%n", horodatage, niveau, nomActeur, message);
    }

    public void info(String message) { log("INFO", message); }
    public void warn(String message) { log("WARN", message); }
    public void error(String message) { log("ERROR", message); }
    public void debug(String message) { log("DEBUG", message); }
}
