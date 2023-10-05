package index;

import java.util.ArrayList;

import storage.Address;

public class LeafNode extends Node {
    private ArrayList<ArrayList<Address>> addressesSet;
    private LeafNode nextLeafNode;

    public LeafNode() {
        super();
        super.setIsLeaf(true);
        super.setIsRoot(false);
        this.addressesSet = new ArrayList<ArrayList<Address>>();
        this.nextLeafNode = null;
    }

    public ArrayList<Address> getAddress(int targetIndex) {
        // Get the Linked List of addresses using index
        ArrayList<Address> targetAddress = addressesSet.get(targetIndex);
        return targetAddress;
    }

    public int setAddress(float targetKey, Address targetAddress) {
        // Set the key and address.
        int addressSetSize = this.getAddresses().size();
        int targetIndex = 0;
        if (addressSetSize > 0) {
            targetIndex = super.setKey(targetKey);
            if (this.getKeys().size() == this.getAddresses().size()) {
                // If the targetIndex already exist.
                // Check the length of keys and length of Address (Should be Same if the
                // targetIndex already Exist).
                // Just insert.
                this.addressesSet.get(targetIndex).add(targetAddress);
            } else {
                // If the targetIndex does not exist.
                // Shift the LinkedList of addresses to the right until the correct position is
                // found and inserted.
                ArrayList<Address> tempLL = new ArrayList<Address>();
                tempLL.add(targetAddress);
                addressesSet.add(tempLL);
                addressSetSize = this.getAddresses().size();
                for (int ptr = addressSetSize - 2; ptr >= targetIndex; ptr--) {
                    addressesSet.set(ptr + 1, addressesSet.get(ptr));
                }
                addressesSet.set(targetIndex, tempLL);
            }

        } else {
            // If the addresses are empty.
            // Initialize new LinkedList of addresses
            ArrayList<Address> tempLL = new ArrayList<Address>();
            tempLL.add(targetAddress);
            this.setKey(targetKey);
            this.addressesSet.add(tempLL);
        }
        return targetIndex;
    }

    public void deleteAddress(int targetIndex) {
        // Delete the key using index.
        // Delete the addresses using index.
        deleteKey(targetIndex);
        addressesSet.remove(targetIndex);
    }

    public ArrayList<ArrayList<Address>> getAddresses() {
        // Getter for addressesSet.
        return this.addressesSet;
    }

    public void deleteAddresses() {
        // Clear out the addresses.
        resetAddresses();
    }

    public void resetAddresses() {
        // Clear out the addresses
        this.addressesSet = new ArrayList<ArrayList<Address>>();
    }

    public LeafNode getNextNode() {
        // Getter for neighbour node.
        return this.nextLeafNode;
    }

    public void setNextNode(LeafNode updateNextLeafNode) {
        // Setter for neighbour node.
        this.nextLeafNode = updateNextLeafNode;
    }

    public void doSeparation() {
        // Delete keys and addresses.
        deleteAllKeys();
        resetAddresses();
    }
}
