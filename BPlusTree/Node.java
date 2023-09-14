package BPlusTree;

import java.util.ArrayList;
import java.util.Collections;

public class Node {
    private InternalNode internalNode;
    private ArrayList<Integer> keys;
    private boolean isRootNode;
    private boolean isLeafNode;

    public Node(){
        isRootNode = true;
        isLeafNode = true;
        keys = new ArrayList<>();
    }

    public InternalNode getInternalNode(){
        return internalNode;
    }

    public void setInternalNode(InternalNode internalNode){
        this.internalNode = internalNode;
    }

    public ArrayList<Integer> getKeys(){
        return keys;
    }

    public int getKey(int i){
        return keys.get(i);
    }

     /**
     setKey(int key): A binary search algorithm that repeatedly divides the search interval in half until the key is
     found or the search interval is empty. In each iteration, the algorithm compares the key to the middle element
     of the search interval and updates the interval accordingly.

     If the key is smaller than the middle element:
        the search interval is updated to the left half of the middle element.
     If the key is larger than the middle element:
        the search interval is updated to the right half of the middle element.
     If the key is equal to the middle element:
        the index of the first occurrence of the key is recorded
     */
    public int setKey(int key){
        if (keys.size() == 0) {
            keys.add(key);
            return 0;
        }
        keys.add(key);
        Collections.sort(keys);
        int low = 0;
        int high = keys.size() - 1;
        int index = -1;

        while (low <= high) {
            // Divides by 2
            int mid = (low + high) >>> 1;
            int valMid = keys.get(mid);

            if (valMid < key) {
                low = mid + 1;
            } else if (valMid > key) {
                high = mid - 1;
            } else {
                index = mid;
                low = mid + 1;
            }
        }
        return index;
    }

    public boolean getIsLeafNode() {
        return isLeafNode;
    }

    public void setIsLeafNode(boolean isitLeafNode) {
        isLeafNode = isitLeafNode;
    }

    public boolean getIsRootNode() {
        return isRootNode;
    }

    public void setIsRootNode(boolean isitRootNode) {
        isRootNode = isitRootNode;
    }

    //delete key in the node
    public void doKeyDeletion(int index) {
        keys.remove(index);
    }

    public void doKeysDeletion() {
        keys = new ArrayList<>();
    }

     /**
     doSmallestKeyRetrieval(): Retrieves the smallest key from a B+ tree.

     If the node is a leaf node:
        the smallest key will be the first key in the node. In this case, the method retrieves the first key
        of the current node and returns it as the smallest key.
     If the node is an internal node (non-leaf node):
        Traverse down the tree to the left-most child of the tree until it reaches a leaf node.
        This is achieved by checking if the left-most child of the current node is a leaf node.
        If it is not a leaf node, the method continues traversing down the tree by assigning the left-most
        child of the current node as the new current node. Once the method reaches a leaf node, it retrieves
        the first key of the left-most child of the current node and returns it as the smallest key.
     */
    public int doSmallestKeyRetrieval(){
        int smallestKey;
        InternalNode intNode;

        if (isLeafNode){
            smallestKey = this.getKey(0);
        }
        else {

            intNode = (InternalNode) this;

            while (!intNode.getChildNode(0).getIsLeafNode()){
                intNode = (InternalNode) intNode.getChildNode(0);
            }
            smallestKey = intNode.getChildNode(0).getKey(0);
        }
        return smallestKey;
    }

    /**
    doNodeDeletion(): deletes a node from a B+ tree.

    If the node has an internal node:
        Deletes itself and sets the internal node as null by calling the doChildNodeDeletion() method
        of the internal node and passing itself as a parameter. After the deletion, the internal node
        of the node is set to null.
    If the node IS a leaf node:
        Casts itself as a LeafNode and calls the deleteAddresses() method of the LeafNode object
        to delete all the addresses. Then, it sets the next node of the LeafNode object to null.
    If the node IS an internal node:
        Casts itself as an InternalNode and calls the doAllChildNodesDeletion() method of the InternalNode
        object to delete all its child nodes.

    After the deletion of the node and its descendants, the method resets the node to its original state.
    Sets the isRootNode and isLeafNode flags to true and initializing the keys list to a new empty list.
    */
    public void doNodeDeletion(){
        if (internalNode != null){
            internalNode.doChildNodeDeletion(this);
            internalNode = null;
        }
        if (isLeafNode){
            LeafNode leafNode = (LeafNode) this;
            leafNode.deleteAddresses();
            leafNode.setNextNode(null);
        }
        else{
            InternalNode intNode = (InternalNode) this;
            intNode.doAllChildNodesDeletion();
        }

        isRootNode = true;
        isLeafNode = true;
        keys = new ArrayList<>();
    }
}
