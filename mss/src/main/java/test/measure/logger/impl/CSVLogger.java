package test.measure.logger.impl;

import test.measure.logger.ILogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Logger that generates a csv file
 */
public class CSVLogger implements ILogger {

    //buffered stream to file
    private BufferedWriter logF;

    public CSVLogger(String file) throws IOException {
        logF = new BufferedWriter( new FileWriter(file));
    }

    @Override
    //pushes the header to the file seperated with ;
    public void init(String... names) throws IOException {
        for (int i = 0; i < names.length; i++) {
            logF.write(names[i]);
            if(i == names.length-1){
                logF.newLine();
            } else {
                logF.write(";");
            }
        }
    }

    @Override
    //pushes values to the file seperated with
    public void log(Object... vals) throws IOException {
        for (int i = 0; i < vals.length; i++) {
            logF.write(vals[i].toString());
            if(i == vals.length-1){
                logF.newLine();
            } else {
                logF.write(";");
            }
        }
    }

    @Override
    public void finish() throws IOException {
        logF.close();                           //close the file
    }
}
