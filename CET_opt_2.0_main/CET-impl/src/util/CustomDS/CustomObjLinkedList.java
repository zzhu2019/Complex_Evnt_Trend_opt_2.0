package src.util.CustomDS;

public class CustomObjLinkedList {
    private LinkedListNode head;
    private int counter;

    public CustomObjLinkedList() {
        head = null;
        counter = 0;
    }

    public LinkedListNode getHead() {
        return head;
    }

    public void addAtHead(int[] data) {
        if(head == null) {
            head = new LinkedListNode(data);
            return;
        }

        LinkedListNode node = new LinkedListNode(data);
        node.setNext(head);
        head = node;
        counter++;
    }

    public void remove() {
        if(head != null) {
            head = head.getNext();
            counter--;
        }
        else {
            System.out.println("No node to be removed");
        }
    }

    private int getCounter() {
        return counter;
    }
}
