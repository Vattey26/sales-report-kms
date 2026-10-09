package main.java.edu.itc.salesreport.ingest;

import main.java.edu.itc.salesreport.model.SaleTransaction;

public interface TransactionParser {
    SaleTransaction parse(String line) throws InvalidRowException;
}
