package test.measure;

/**
 * Interface to allow classes to make their stored hash values countable
 */
public interface SpaceMeasureable {
    //resets theis class (for next run)
    public void unMark();
    //counts the values and marks them so that they are counted only once
    public int markAndCount();
}
