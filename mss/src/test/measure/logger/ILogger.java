package test.measure.logger;

import java.io.IOException;

/**
 * Provides a Channel to log measured values
 */
public interface ILogger {
    //name of columns for measuring multiple values together
    public void init(String... names) throws IOException;
    //datapoints per column for measuring multiple values together
    public void log(Object... vals) throws IOException;
    //all datapoints where submitted so clean up
    public void finish() throws IOException;
}
