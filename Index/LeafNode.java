package Index;

import Storage.Address;
import java.util.ArrayList;

public class LeafNode extends Node {
    private ArrayList<Address> addressesSet;
    private LeafNode nextLeafNode;

    public LeafNode() {
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

    public int setAddress(float targetKey, Address targetAddress) {
        int addressSetSize = this.getAddresses().size();
        int targetIndex = 0;
        if (addressSetSize > 0) {
            targetIndex = super.setKey(targetKey);
            addressesSet.add(targetAddress);
            addressSetSize = this.getAddresses().size();

            for (int ptr = addressSetSize - 2; ptr >= targetIndex; ptr--) {
                addressesSet.set(ptr + 1, addressesSet.get(ptr));
            }
            addressesSet.set(targetIndex, targetAddress);

        } else {
            this.setKey(targetKey);
            this.addressesSet.add(targetAddress);
        }
        return targetIndex;
    }

    public void deleteAddress(int targetIndex) {
        deleteKey(targetIndex);
        addressesSet.remove(targetIndex);
    }

    public void deleteAddresses() {
        resetAddresses();
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

    public void resetAddresses() {
        this.addressesSet = new ArrayList<Address>();
    }

}
