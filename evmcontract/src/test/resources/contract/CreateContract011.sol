pragma solidity ^0.4.12;

contract DataStore {
    uint256 data;

    function set(uint256 x) public {
        data = x;
    }

    function get() public view returns (uint256) {
        return data;
    }
}

contract Caller {
    address ds;

    function call() public {
        ds = new DataStore();
        DataStore dataStore = DataStore(ds);
        dataStore.set(6);
    }

    function getDsAddress() public view returns (address) {
        return ds;
    }
}
