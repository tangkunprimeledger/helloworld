pragma solidity ^0.4.12;

import "./StandardToken.sol";
import "./TokenReceiver.sol";

contract StandardCurrency is StandardToken {

    constructor (
        address _offerAddr,
        string _tokenName,
        string _tokenSymbol,
        uint _initNum,
        uint8 _decimals,
        address _frozeContractAddress
    ) public {
        offerAddress = _offerAddr;
        tokenName = _tokenName;
        tokenSymbol = _tokenSymbol;
        decimalsDigit = _decimals;
        totalSupplyAmount = _initNum * 10 ** uint(decimalsDigit);
        balance[offerAddress] = totalSupplyAmount;
        frozeContractAddress = _frozeContractAddress;
    }

    mapping(address => uint) balance;   //存储所有拥有此稳定币的地址与余额的映射

    //查询余额及冻结金额
    function balanceOf(address _owner) public view returns (uint balanceAmount, uint frozenAmount){
        balanceAmount = balance[_owner];
        frozenAmount = getFrozenAmount(_owner);
        return (balanceAmount, frozenAmount);
    }

    //查询余额
    function getBalance(address _owner) internal view returns (uint){
        return balance[_owner];
    }

    //增发
    //num：增发的数量（合约内需要换算为最低单位）
    function additionalIssue(uint num) public returns (bool){
        require(msg.sender == offerAddress, "msg.sender is not offer address");
        num = num * 10 ** uint(decimalsDigit);
        totalSupplyAmount += num;
        balance[offerAddress] += num;
        return true;
    }

    //转账接口
    //_from：msg.sender
    //_to：接收方地址
    //_value：转账金额
    function transfer(address _to, uint256 _value) public payable returns (bool){
        return transferFrom(msg.sender, _to, _value);
    }

    //转账接口
    //_from：转出方地址
    //_to：接收方地址
    //_value：转账金额
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool){
        //转出方必须未被冻结
        require(tokenCheck(_from), "from address has been frozen");
        //防止给零地址转账
        require(_to != 0x0, "to address is 0x0");
        //转账金额必须大于0
        require(_value > 0, "the value must be that is greater than zero.");
        //转出方余额必须足够(所有余额减去冻结金额)
        require(balance[_from] - getFrozenAmount(_from) >= _value, "balance not enough");
        //检查是否溢出
        require(balance[_to] + _value >= balance[_to], "to address balance overflow");
        //为后面作断言使用
        uint previousBalance = balance[_from] + balance[_to];
        //转出方减去转出金额
        balance[_from] -= _value;
        //接收方余额加上转账金额
        balance[_to] += _value;
        //通知订阅了此事件的客户端
        emit Transfer(_from, _to, _value);
        //断言转账前后，转出方，接收方余额之和相等。
        assert(balance[_from] + balance[_to] == previousBalance);

        return true;
    }

    /// @param _to The address of the contract
    /// @param _payment cost of stable token to subscribe
    /// @param _amount The amount of token to be transferred/// @param _amount The amount of token to be transferred
    /// @return Whether the subscribe is successful or not
    function transferToContract(address _to, uint256 _payment, uint256 _amount) public returns (bool success) {
        require(_payment <= getBalance(msg.sender), "balance not enough");
        require(_to != address(0), "to address illegal");

        TokenReceiver tokenReceiver = TokenReceiver(_to);
        address contractOfferAddress = tokenReceiver.tokenOfferAddress();
        success = transferFrom(msg.sender, contractOfferAddress, _payment);
        if (success) {
            success = tokenReceiver.receiveToken(msg.sender, _payment, _amount);
            emit RechangeSuccess(msg.sender, success);
            if (!success) {
                rollbackTransfer(msg.sender, contractOfferAddress, _payment);
            }
        }
        return success;
    }

    // rollback of transfer when failure
    function rollbackTransfer(address _from, address _to, uint _value) private {
        balance[_to] -= _value;
        balance[_to] += _value;
        emit RollbackTransfer(_to, _from, _value);
    }

    //冻结某个地址上的金额
    //freezeAddr：冻结地址
    //freezeNum: 冻结金额数量
    function freeze(address freezeAddr, uint freezeNum) public returns (bool){
        //只有发行方能操作
        require(msg.sender == offerAddress, "msg.sender is not offer address");
        require(freezeNum > 0, "The value must be that is greater than zero.");
        //余额必须大于等于冻结金额
        require(balance[freezeAddr] >= freezeNum, "The balance must be greater than or equal to the amount to be frozen");
        frozenAddressAmount[freezeAddr] += freezeNum;
        return true;
    }

    event RechangeSuccess(address indexed _to, bool success);
    event RollbackTransfer(address indexed _from, address indexed _to, uint256 _value);
}