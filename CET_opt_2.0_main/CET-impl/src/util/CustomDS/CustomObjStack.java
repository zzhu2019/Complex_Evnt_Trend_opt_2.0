package src.util.CustomDS;

import java.util.Arrays;

/***
 * A customised stack
 * @param <E> The dynamic type
 */
public class CustomObjStack<E> {
    private int top;
    private Object[] stack;

    /***
     * Constructor
     * @param size The size of the stack after the initiation
     */
    public CustomObjStack(int size) {
        top = -1;
        stack = new Object[size];
        Arrays.fill(stack, -1);
    }

    /***
     * Constructor for not specifying the stack initial size
     */
    public CustomObjStack() {
        this(10);
    }

    /***
     * If the stack is empty
     * @return
     */
    public boolean isEmpty() {
        return (top < 0);
    }

    /***
     * Push an element
     * @param x
     */
    public void push(E x) {

        if(top >= stack.length - 1) {
            Object[] newStack = new Object[(int) (1.75 * stack.length) + 1];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[++top] = x;
    }

    /***
     * Return the size of stack
     * @return size
     */
    public int size(){
        return top+1;
    }

    /***
     * Pop out the stack top element
     * @return the top element
     */
    public E pop() {
        if(top < 0) {
            throw new NumberFormatException();
        }

        return (E) stack[top--];

    }

    /***
     * Return the stack top element
     * @return the stack top element
     */
    public E peek() {
        if(top < 0) {

            throw new ArrayIndexOutOfBoundsException();
        }

        return (E) stack[top];

    }

    /***
     * Return the stack bottom element
     * @return the stack bottom element
     */
    public E firstElement() {
        if(top < 0)
            throw new NumberFormatException();
        return (E) stack[0];
    }

    /***
     * Return the stack top element
     * @return the stack top element
     */
    public E lastElement() {
        if(top < 0) {
            throw new NumberFormatException();
        }
        return (E) stack[top];
    }

    /***
     * Return the stack as an object array
     * @return an object array
     */
    public Object[] getAllElements() {
        // Time complexity <= O(length)
        return Arrays.copyOfRange(stack, 0, top + 1);
    }
}
