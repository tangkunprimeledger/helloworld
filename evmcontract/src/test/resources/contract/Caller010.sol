pragma solidity ^0.4.12;

contract DataStore {
    function set(uint256 x) public;
}

contract Caller {
    function call() public {
        address addr = 0x00a615668486da40f31fd050854fb137b317e056;
        DataStore dataStore = DataStore(addr);
        dataStore.set(6);
    }
}