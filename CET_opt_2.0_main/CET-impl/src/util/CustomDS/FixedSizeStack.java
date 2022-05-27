package src.util.CustomDS;

import java.util.Arrays;

public class FixedSizeStack<E> {
    private int top;
    private Object[] stack;


    public FixedSizeStack(int size) {
        top = -1;
        stack = new Object[size];
        Arrays.fill(stack, -1);
    }

    public FixedSizeStack() {
        this(10);
    }


    public boolean isEmpty() {
        return (top < 0);
    }


    public void push(E x) {
        if(top >= stack.length - 1) {
//            throw new ArrayIndexOutOfBoundsException();
            Object[] newStack = new Object[(int) (1.75 * stack.length) + 1];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[++top] = x;
    }

    public int size(){
        return top+1;
    }

    public E pop() {
        if (top < 0)
            throw new NumberFormatException();

        return (E) stack[top--];

    }

    public E peek() {
        if (top < 0)
            throw new ArrayIndexOutOfBoundsException();

        return (E) stack[top];

    }

    public Object firstElement() {
        if (top < 0)
            throw new NumberFormatException();
        return stack[0];
    }

    public Object[] getAllElements() {
        // Time complexity <= O(length)
        return Arrays.copyOfRange(stack, 0, top + 1);
    }
}
