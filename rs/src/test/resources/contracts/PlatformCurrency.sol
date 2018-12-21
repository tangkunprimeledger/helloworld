pragma solidity ^0.4.12;

import "./SafeMath.sol";
import "./StandardCurrency.sol";

/**
 * @author suimi
 */
contract PlatformCurrency is StandardCurrency {

    using SafeMath for uint256;

    string public name;
    string public symbol;
    uint8 public decimals;
    uint256 public totalSupply;

    // This creates an array with all balances
    mapping(address => uint256) balances;

    function PlatformCurrency(){
        name = "GDollar";
        symbol = "GD";
        decimals = 8;
        totalSupply = 1000000000000;
        balances[0x0] = totalSupply;
    }
    //名称
    function name() public view returns (string){
        return name;
    }

    //标识
    function symbol() public view returns (string){
        return symbol;
    }

    //小数位
    function decimals() public view returns (uint8){
        return decimals;
    }

    //总发行量
    function totalSupply() public view returns (uint256){
        return totalSupply;
    }

    //查询余额
    function balanceOf(address _owner) public view returns (uint256 balance){
        balance = balances[_owner];
        return balance;
    }

    function transfer(address _to, uint256 _value) public returns (bool success){
        return transferFrom(0x0, _to, _value);
    }

    function transferFrom(address _from, address _to, uint256 _value) public returns (bool success){
        require(_value <= balances[_from]);
        require(_to != address(0));
        require(_value > 0);

        balances[_from] = balances[_from].sub(_value);
        balances[_to] = balances[_to].add(_value);
        emit Transfer(_from, _to, _value);
        return true;
    }

    function additionalIssue(uint num) public returns (bool success){
        balances[0x0] = balances[0x0].add(num);
        totalSupply = totalSupply.add(num);
        return true;
    }
}