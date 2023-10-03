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
        ArrayList<Address> targetAddress = addressesSet.get(targetIndex);
        return targetAddress;
    }

    public int setAddress(float targetKey, Address targetAddress) {
        int addressSetSize = this.getAddresses().size();
        int targetIndex = 0;
        if (addressSetSize > 0) {
            targetIndex = super.setKey(targetKey);
            // If the targetIndex already exist
            // Check the length of keys and length of Address (Should be Same if the
            // targetIndex already Exist)
            // Just insert
            if (this.getKeys().size() == this.getAddresses().size()) {
                this.addressesSet.get(targetIndex).add(targetAddress);
            } else {
                // If the targetIndex does not exist
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
            ArrayList<Address> tempLL = new ArrayList<Address>();
            tempLL.add(targetAddress);
            this.setKey(targetKey);
            this.addressesSet.add(tempLL);
        }
        return targetIndex;
    }

    public void deleteAddress(int targetIndex) {
        deleteKey(targetIndex);
        addressesSet.remove(targetIndex);
    }

    public ArrayList<ArrayList<Address>> getAddresses() {
        return this.addressesSet;
    }

    public void deleteAddresses() {
        resetAddresses();
    }

    public void resetAddresses() {
        this.addressesSet = new ArrayList<ArrayList<Address>>();
    }

    public LeafNode getNextNode() {
        return this.nextLeafNode;
    }

    public void setNextNode(LeafNode updateNextLeafNode) {
        this.nextLeafNode = updateNextLeafNode;
    }

    public void doSeparation() {
        deleteAllKeys();
        resetAddresses();
    }
}
