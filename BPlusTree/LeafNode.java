package BPlusTree;

import Storage.Address;
import java.util.ArrayList;

public class LeafNode extends Node{
    private ArrayList<Address> addresses;
    private LeafNode nextNode;

    public LeafNode(){
        super();
        setIsRootNode(false);
        setIsLeafNode(true);
        addresses = new ArrayList<Address>();
        setNextNode(null);
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public Address getAddress(int i) {
        return addresses.get(i);
    }

    /**
    SetAddress(int key, Address address): sets an address for a given key in a leaf node (current node).

    If the node is empty:
        sets the key and address at index 0
    Else, it uses the setKey method from the Node class to set the key at the appropriate index and
    adds the address to the addresses list at the same index.
    The method then performs a for loop to shift all the subsequent addresses to the right to make room for
    the new address at the given index.
    Finally, it sets the index and address in the addresses list of the current node.
    Returns the index at which the key was inserted.
     */
    public int setAddress(int key, Address address){
        if (this.getAddresses().size() == 0) {
            this.setKey(key);
            this.addresses.add(address);
            return 0;
        }
        int index = super.setKey(key);
        addresses.add(address);

        for (int i = addresses.size() -2; i >= index; i--)
            addresses.set(i+1, addresses.get(i));

        addresses.set(index, address);
        return index;
    }


    public void deleteAddress(int i) {
        doKeyDeletion(i);
        addresses.remove(i);
    }

    public void deleteAddresses() {
        addresses = new ArrayList<Address>();
    }

    public LeafNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(LeafNode nextNode) {
        this.nextNode = nextNode;
    }

    /**
    doSeparation(): Deletes all keys and resets all the addresses
    */
    public void doSeparation(){
        doKeysDeletion();
        addresses = new ArrayList<Address>();
    }

}
