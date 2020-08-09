package suit.algorithms.generate.tree.single.advanced.queue;

import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;


//eventually do H-Tree because we can not yet reduce arrays

/**
 * Fixed Size Array based implementation of the RetainQueue
 * Exactly size enques followed by size dequees have to be called
 */
public class ArrayRetainQueue implements RetainQueue {
    DataArray[] elems;                      //array with the elements
    int index;                              //next free slot or next full slot depending on mode
    boolean fillMode = true;                //true when enques are called, false when deques are called

    public ArrayRetainQueue(int size) {
        elems = new DataArray[size];
        index = size - 1;
    }

    @Override
    //for space mesurement
    public void unMark() {
        for (DataArray elem: elems){
            if(elem == null) break;
            RefCountDataArray.prepare(elem);
        }
    }

    @Override
    //for space mesurement
    public int markAndCount() {
        int sum = 0;
        for (DataArray elem: elems){
            if(elem == null) break;
            sum += RefCountDataArray.count(elem);
        }
        return sum;
    }

    public void enqueue(DataArray data){
        assert(fillMode);               //make sure mode is right
        elems[index--] = data;          //fill the slot
    }

    public DataArray dequeue(){
        if(fillMode){                  //check if this is the mode switch
            assert(index == -1);       //check that enque was called size times
            index = elems.length - 1;  //reset pointer to first  enqued element
            fillMode = false;          //set new mode
        }
        DataArray ret = elems[index];  //retrive item
        elems[index--] = null;         //clear item and set pointer to next item
        //it would be nice if we already have dynamically length reducable arrays ( if we had we would reduce here by one)
        return ret;
    }
}
