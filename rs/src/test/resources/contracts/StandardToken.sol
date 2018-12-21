pragma solidity ^0.4.12;


contract Froze {
    function check(address account, uint256 currencyType) public returns (bool);
}

contract StandardToken {

    address offerAddress;       //发行人地址
    string tokenName;          //token名称（如Bitcoin）
    string tokenSymbol;        //token符号（如BTC）
    uint totalSupplyAmount;          //发行数量 (用最低单位表示)
    uint8 decimalsDigit;             //小数点位数
    address frozeContractAddress;  //冻结合约地址(构造函数传入)

    mapping(address => uint) frozenAddressAmount;   //某个地址冻结的金额映射

    //获取某个地址冻结金额
    function getFrozenAmount(address freezeAddr) public view returns (uint){
        return frozenAddressAmount[freezeAddr];
    }

    //发行方账户地址
    function offerAddr() public view returns (address){
        return offerAddress;
    }

    //名称
    function name() public view returns (string){
        return tokenName;
    }
    //标识
    function symbol() public view returns (string){
        return tokenSymbol;
    }
    //小数位
    function decimals() public view returns (uint8){
        return decimalsDigit;
    }
    //总发行量
    function totalSupply() public view returns (uint256){
        return totalSupplyAmount;
    }

    //冻结合约地址
    function getFrozenContractAddress() public view returns (address){
        return frozeContractAddress;
    }

    function transfer(address _to, uint256 _value) public payable returns (bool success);

    function additionalIssue(uint num) public returns (bool success);

    event Transfer(address indexed from, address indexed to, uint256 value);

    //解冻某个地址上的金额
    //unfreezeAddr：解冻地址
    //freezeAmount：解冻金额
    function unfreeze(address unfreezeAddr, uint unfreezeAmount) public returns (bool){
        //只有发行方能操作
        require(msg.sender == offerAddress, "msg.sender is not offer address");
        require(unfreezeAmount > 0, "unfreeze amount must be greater than 0");
        //此地址冻结金额必须大于0
        require(frozenAddressAmount[unfreezeAddr] > 0, "There is no amount frozen at this address");
        require(unfreezeAmount <= frozenAddressAmount[unfreezeAddr], "unfreeze amount is greater than frozen amount");
        frozenAddressAmount[unfreezeAddr] -= unfreezeAmount;
        return true;
    }

    //解冻某个地址下所有冻结金额
    function unfreezeAllAmount(address unfreezeAddr) public returns (bool){
        //只有发行方操作
        require(msg.sender == offerAddress, "msg.sender is not offer address");
        require(frozenAddressAmount[unfreezeAddr] > 0, "There is no amount frozen at this address");
        frozenAddressAmount[unfreezeAddr] = 0;
        return true;
    }


    function stoCheck(address addr) public returns (bool){
        require(frozeContractAddress != 0x0);
        Froze froze = Froze(frozeContractAddress);
        return froze.check(addr, 2);
    }

    function tokenCheck(address addr) public returns (bool){
        require(frozeContractAddress != 0x0);
        Froze froze = Froze(frozeContractAddress);
        return froze.check(addr, 3);
    }
}
