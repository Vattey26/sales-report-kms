package main.java.edu.itc.salesreport.ingest;

public class InvalidRowException extends Exception {
    public InvalidRowException(String message) {
        super(message);
    }

    public InvalidRowException(String message, Throwable cause) {
        super(message, cause);
    }
}