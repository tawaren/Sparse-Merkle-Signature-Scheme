package test.measure.logger.impl;

import test.measure.logger.ILogger;

/**
 * A logger, that simply puts the values to the console
 */
public class ConsoleLogger implements ILogger {

    //the names of the column
    String[] headers;


    @Override
    public void init(String... names) {
        headers = names;
    }

    @Override
    public void log(Object... vals) {

        if(headers != null){
            StringBuilder strB = new StringBuilder();                       //Stringbuilder for concatenation
            for(int i = 0; i  < headers.length && i < vals.length; i++){    //append columnName:value pairs
                strB.append(headers[i]);
                strB.append(":");
                strB.append(vals[i]);
                strB.append(" ");
            }
            System.out.println(strB.toString());                            //push to the console
        }
    }

    @Override
    public void finish() {
        headers = null;                 //clean the headers
    }
}
