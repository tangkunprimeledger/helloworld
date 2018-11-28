pragma solidity ^0.4.25;

contract Test {
    mapping(uint => uint) public data;
    mapping(address => uint) public users;
    address[] public userList;

    constructor(address[] _userList, uint[] userAmountList) public {
        for(uint i = 0; i < _userList.length; i++) {
            userList.push(_userList[i]);
            users[_userList[i]] = userAmountList[i];
        }
    }

    function initData() public {
        for (uint i = 1; i < 10000; i++) {
            data[i] = i;
        }
    }

    function putVal(uint key, uint val) public {
        data[key] = val;
        return;
    }

    function balanceOf(address account) public view returns (uint) {
        return users[account];
    }

    function getTotalBalance() public view returns (uint) {
        uint sum = 0;
        for(uint i = 0; i < userList.length; i++) {
            sum = sum + users[userList[i]];
        }
        return sum;
    }

    function getUserInfo() public view returns (address[], uint[], string, uint) {
        //address[] memory _userList = new uint[](2);
        uint[] memory userAmountList = new uint[](userList.length);
        for(uint i = 0; i < userList.length; i++) {
            userAmountList[i] = users[userList[i]];
        }
        return (userList, userAmountList, "higgstrust", 1000000);
    }
}