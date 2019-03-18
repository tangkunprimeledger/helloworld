pragma solidity ^0.4.12;

/**
 * @author suimi
 */
interface CurrencyContract {function transferFrom(address _from, address _to, uint256 _value) public returns (bool success);}

contract SettlementInterest {

    function SettlementInterest(){
    }

    function settl(address currencyAddr, address _from, address[] _addrs, uint256[] _values) public returns (bool success){
        require(_addrs.length == _values.length);
        require(isContract(currencyAddr));
        CurrencyContract currency = CurrencyContract(currencyAddr);
        for (uint16 i = 0; i < _addrs.length; i++) {
            currency.transferFrom(_from, _addrs[i], _values[i]);
        }
        return true;
    }

    //assemble the given address bytecode. If bytecode exists then the _addr is a contract.
    function isContract(address _addr) private view returns (bool is_contract) {
        uint length;
        assembly {
        //retrieve the size of the code on target address, this needs assembly
            length := extcodesize(_addr)
        }
        return (length > 0);
    }
}