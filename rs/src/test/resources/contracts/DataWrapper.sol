pragma solidity ^0.4.24;

contract DataWrapper {
    uint256 data;

    function addOne() public {
        data = data + 1;
    }

    function get() public view returns (uint256) {
        return data;
    }
}