package src.util.CustomDS;

public class LinkedListNode {
    private final int[] data;
    private LinkedListNode next;

    public LinkedListNode(int[] data) {
        this.data = data;
        next = null;
    }

    public void setNext(LinkedListNode next) {
        this.next = next;
    }

    public LinkedListNode getNext() {
        return next;
    }

    public int[] getData() {
        return data;
    }
}
