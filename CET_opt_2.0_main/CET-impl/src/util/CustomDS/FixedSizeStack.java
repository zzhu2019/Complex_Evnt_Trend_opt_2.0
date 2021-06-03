package src.util.CustomDS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FixedSizeStack {
    private int top;
    private final int maxSize;
    private final List<int[]> stack;


    public FixedSizeStack(int size) {
        top = -1;
        maxSize = size;
        stack = new ArrayList<>(size);
    }

    public FixedSizeStack() {
        this(10);
    }


    public boolean isEmpty() {
        return (top < 0);
    }


    public void push(int[] x) {
        if(top >= maxSize - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if(stack.size()-1 <= top) stack.add(++top, x);
        else stack.set(++top, x);
    }

    public int size(){
        return top+1;
    }

    public int[] pop() {
        if(top < 0) {
            throw new NumberFormatException();
        }
        // TODO: possible memory leak here
        return stack.get(top--);

    }

    public int[] peek() {
        if(top < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return stack.get(top);

    }

    public Object firstElement() {
        if(top < 0) {
            throw new NumberFormatException();
        }
        return stack.get(0);
    }

//    public List<int[]> getAllElements() {
//        // Time complexity <= O(length)
//        return Arrays.copyOfRange(stack, 0, top + 1);
//    }

    public Iterator<int[]> getIterator() {
        return stack.iterator();
    }
}
