pragma solidity ^0.4.12;

contract ExecutorTest009 {
    event Sent(uint indexed value, address from, uint amount);

    constructor () public {
        emit Sent(25, msg.sender, 56);
    }
}