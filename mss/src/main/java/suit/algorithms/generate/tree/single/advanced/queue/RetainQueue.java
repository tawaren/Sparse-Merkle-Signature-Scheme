package suit.algorithms.generate.tree.single.advanced.queue;

import suit.tools.arrays.DataArray;
import test.measure.SpaceMeasureable;

/**
 * Interface for the Queue used to store and retain values at the top K levels of a logSpaceTime Tree
 * first all enqueues have to be called then all dequeues
 */
public interface RetainQueue extends SpaceMeasureable {
    public void enqueue(DataArray data);
    public DataArray dequeue();
}
