package src.util.CustomDS;

import java.util.Arrays;

public class ShortArray {
    private int currentPos;
    private short[] queue;

    /**
     * Standard constructor. Constructs an array of size 10 if no size is provided.
     */
    public ShortArray() {
        this(10);
    }

    public ShortArray(ShortArray newArray) {
        queue = new short[newArray.size() + 10];
        Arrays.fill(queue, (short) -1);
        System.arraycopy(newArray.getWholeArray(), 0, queue, 0, newArray.size());
        currentPos = newArray.size();

    }

    /**
     * Constructs an arraylist of the specified size.
     *
     * @param startSize initial capacity
     */
    public ShortArray(int startSize) {

        this.queue = new short[startSize];
        Arrays.fill(queue, (short) -1);
        this.currentPos = 0;
    }

    /**
     * Returns size of the queue after startPos.
     * Note: Can't use ArrayList.size because it may contain elements before startPos.
     *
     * @return Size of queue.
     */
    public int size() {
        return currentPos;
    }

    /**
     * Checks if the queue is empty.
     *
     * @return true if empty, false if else.
     */
    public boolean isEmpty() {
        return currentPos == 0;
    }


    /**
     * Converts queue to array.
     *
     * @return Queue in array form.
     */
    public short[] getArray() {
        return Arrays.copyOfRange(queue, 0, currentPos);
    }

    public short[] getWholeArray() {
        return queue;
    }


    /**
     * Clears the queue.
     */
    public void clear() {
        Arrays.fill(queue, (short) -1);
        currentPos = 0;
    }


    /**
     * Resizes the queue, but only carries over elements from before the start position.
     */
    private void resize() {
        short[] newQueue = new short[(int) (queue.length * 1.75) + 1];
        Arrays.fill(newQueue, (short) -1);

        System.arraycopy(queue, 0, newQueue, 0, queue.length);
        currentPos = queue.length;
        queue = newQueue;

    }

    /**
     * Adds the object to the end of the queue. Throws NullPointerException if element is null.
     *
     * @param i int to be added.
     */
    public void add(short i) {

        if(i < 0) throw new NumberFormatException();

        queue[currentPos++] = i;
        if (currentPos == queue.length) resize();

    }


    /**
     * Adds the object to the end of the queue. Throws NullPointerException if element is null.
     *
     * @param pos int to get.
     */
    private int get(int pos) {
        if (pos > currentPos) throw new NumberFormatException();

        return queue[pos];
    }

    public int getLast() {
        return queue[currentPos - 1];
    }
}
