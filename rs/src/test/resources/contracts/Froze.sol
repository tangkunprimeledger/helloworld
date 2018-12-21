pragma solidity ^0.4.12;

contract Froze {

    address _address = msg.sender;
    mapping(address => uint256) private stoFrozen;
    mapping(address => uint256) private tokenFrozen;

    function frozeAccount(address account, uint256 currency, uint256 endTime) public returns (bool){
        require(msg.sender == _address, "msg.sender is not deploy address");
        if (currency == 1) {// all type froze

            if (stoFrozen[account] > block.timestamp || tokenFrozen[account] > block.timestamp) {
                return false;
            }
            stoFrozen[account] = endTime;
            tokenFrozen[account] = endTime;
        } else if (currency == 2) {// sto froze
            if (stoFrozen[account] > block.timestamp) {
                return false;
            }
            stoFrozen[account] = endTime;
        } else if (currency == 3) {// token froze
            if (tokenFrozen[account] > block.timestamp) {
                return false;
            }
            tokenFrozen[account] = endTime;
        } else {
            return false;
        }
        return true;
    }

    function unfrozeAccount(address account, uint256 currency) public returns (bool){
        require(msg.sender == _address, "msg.sender is not deploy address");
        if (currency == 1) {
            stoFrozen[account] = 0;
            tokenFrozen[account] = 0;
        } else if (currency == 2) {
            stoFrozen[account] = 0;
        } else if (currency == 3) {
            tokenFrozen[account] = 0;
        }
        return true;
    }


    function check(address account, uint256 currencyType) public view returns (bool){

        if (currencyType == 3 && block.timestamp < tokenFrozen[account]) {
            return false;
        } else if (currencyType == 2 && block.timestamp < stoFrozen[account]) {
            return false;
        }
        return true;
    }

}
