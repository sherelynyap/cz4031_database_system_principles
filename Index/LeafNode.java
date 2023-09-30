package Index;

import Storage.Address;
import java.util.ArrayList;

public class LeafNode extends Node {
    private ArrayList<Address> addressesSet;
    private LeafNode nextLeafNode;

    public LeafNode() {
        // Refactored
        super();
        super.setIsRootNode(false);
        super.setIsLeafNode(true);
        this.addressesSet = new ArrayList<Address>();
        this.nextLeafNode = null;
    }

    public ArrayList<Address> getAddresses() {
        return this.addressesSet;
    }

    public Address getAddress(int targetIndex) {
        Address targetAddress = addressesSet.get(targetIndex);
        return targetAddress;
    }

    /**
     * SetAddress(int key, Address address): sets an address for a given key in a
     * leaf node (current node).
     * 
     * If the node is empty:
     * sets the key and address at index 0
     * Else, it uses the setKey method from the Node class to set the key at the
     * appropriate index and
     * adds the address to the addresses list at the same index.
     * The method then performs a for loop to shift all the subsequent addresses to
     * the right to make room for
     * the new address at the given index.
     * Finally, it sets the index and address in the addresses list of the current
     * node.
     * Returns the index at which the key was inserted.
     */
    public int setAddress(float targetKey, Address targetAddress) {
        int addressSetSize = this.getAddresses().size();
        int targetIndex = 0;
        if (addressSetSize == 0) {
            this.setKey(targetKey);
            this.addressesSet.add(targetAddress);
            // return 0;
        } else {
            targetIndex = super.setKey(targetKey);
            addressesSet.add(targetAddress);
            addressSetSize = this.getAddresses().size();

            for (int ptr = addressSetSize - 2; ptr >= targetIndex; ptr--)
                addressesSet.set(ptr + 1, addressesSet.get(ptr));
            addressesSet.set(targetIndex, targetAddress);
            // return targetIndex;
        }
        return targetIndex;
    }

    public void deleteAddress(int targetIndex) {
        deleteKey(targetIndex);
        addressesSet.remove(targetIndex);
    }

    public void deleteAddresses() {
        // addresses = new ArrayList<Address>();
        resetAddresses();
    }

    public LeafNode getNextNode() {
        return this.nextLeafNode;
    }

    public void setNextNode(LeafNode updateNextLeafNode) {
        this.nextLeafNode = updateNextLeafNode;
    }

    /**
     * doSeparation(): Deletes all keys and resets all the addresses
     */
    public void doSeparation() {
        deleteAllKeys();
        resetAddresses();
    }

    public void resetAddresses() {
        this.addressesSet = new ArrayList<Address>();
    }

}
