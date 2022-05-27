package src.util.CustomDS;

import java.util.Arrays;

public class CustomIntStack {

    private int top;
    private int[] stack;

    /**
     * the constructor
     * @param size: the size of the stack
     */
    public CustomIntStack(int size) {
        top = -1;
        stack = new int[size];
        Arrays.fill(stack, -1);
    }

    /**
     * the constructor with the default size = 10
     */
    public CustomIntStack() {
        this(10);
    }

    /**
     * Is the stack is empty?
     * @return true of false
     */
    public boolean isEmpty() {
        return (top < 0);
    }

    /**
     * Push an element into the stack
     * @param x: the to-be-pushed element
     */
    public void push(int x) {

        if(top >= stack.length - 1) {
            int[] newStack = new int[(int) (1.75 * stack.length) + 1];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[++top] = x;
    }

    /**
     * Pop an element out of the stack
     * @return The popped element
     */
    public int pop() {
        if(top < 0)
            throw new NumberFormatException();

        return stack[top--];
    }

    /**
     * Get the top element in the stack
     * @return the top element
     */
    public int peek() {
        if(top < 0)
            throw new NumberFormatException();

        return stack[top];

    }

    /**
     * Get a copy of this stack
     * @return the copy
     */
    public int[] getAllElements() {
        return Arrays.copyOfRange(stack, 0, top + 1);
    }

    /**
     * Get the stack bottom element
     * @return the bottom element
     */
    public int getFirstElement() {
        if(top < 0)
            throw new NumberFormatException();
        return stack[0];
    }

    /**
     * Get the stack size
     * @return the size
     */
    public int size() {
        return top + 1;
    }
}
