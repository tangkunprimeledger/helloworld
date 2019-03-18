pragma solidity ^0.4.12;

import "./StandardToken.sol";
import "./TokenReceiver.sol";

contract StandardSTO is StandardToken, TokenReceiver {

    string standardCurrencySymbol;     //限制能买此token的稳定币币种
    address stableCurrencyContractAddress; //稳定币币种对应的智能合约
    uint32 exchangeRate;       //稳定币与此token的兑换比例
    uint32 lowestShareNum;        //最低认购数量
    uint maxShareNum;           //最多认购份额,针对某个用户累计购买总额。
    uint totalSubscribedNum;       //总共可认购数量（所有用户累计）
    uint32 subscriptionStartDate;  // 认购开始日期
    uint32 subscriptionEndDate;  // 认购结束日期
    uint16 lockupPeriodDays;        //锁定期时间：天
    mapping(address => bool) allowedAddr;   //购买允许地址集合

    uint16 constant MAX_NUMBER_OF_QUERY_PER_PAGE = 1000;      //每页最大查询数量

    constructor (
        address _offerAddr,
        string _tokenName,
        string _tokenSymbol,
        uint8 _decimals,
        uint _totalSupply,
        string _standardCurrencySymbol,
        uint32 _exchangeRatio,
        uint32 _lowestShareNum,
        uint _maxShareNum,
        uint _totalSubscribedNum,
        uint32 _subscriptionStartDate,
        uint32 _subscriptionEndDate,
        uint16 _lockupPeriodDays,
        address _stableCurrencyContractAddress,
        address _frozeContractAddress
    ) public {
        offerAddress = _offerAddr;
        tokenName = _tokenName;
        tokenSymbol = _tokenSymbol;
        decimalsDigit = _decimals;
        totalSupplyAmount = _totalSupply * 10 ** uint(decimalsDigit);

        frozeContractAddress = _frozeContractAddress;
        standardCurrencySymbol = _standardCurrencySymbol;
        exchangeRate = _exchangeRatio;
        lowestShareNum = _lowestShareNum;
        maxShareNum = _maxShareNum;
        totalSubscribedNum = _totalSubscribedNum;
        subscriptionStartDate = _subscriptionStartDate;
        subscriptionEndDate = _subscriptionEndDate;
        lockupPeriodDays = _lockupPeriodDays;
        //将发行方与发行数量存入此mapping
        balance[offerAddress].balance = totalSupplyAmount;
        stableCurrencyContractAddress = _stableCurrencyContractAddress;
        //将发行人存入地址数组
        addresses.push(offerAddress);
        //将地地址标记为存在
        balance[offerAddress].exists = true;
        //为发行地址添加权限,以方便其他用户可从此地址进行认购
        allowedAddr[offerAddress] = true;
        //冻结除掉“总共可认购金额”剩余的金额
        freeze(offerAddress, totalSupplyAmount - totalSubscribedNum);
    }

    struct Balance {
        uint balance;   //余额
        bool exists;    //是否存在（转账时判断地址是否存在mapping中）
    }

    mapping(address => Balance) balance;   //存储所有拥有此token地址与余额的映射
    address[] addresses;    //维护拥有该token的所有地址

    //稳定币兑换比例
    function exchangeRatio() public view returns (uint32){
        return exchangeRate;
    }
    //稳定币标识
    function GetStandardCurrencySymbol() public view returns (string){
        return standardCurrencySymbol;
    }
    //稳定币合约地址
    function currencyAddr() public view returns (address){
        return stableCurrencyContractAddress;
    }
    //最低认购份额
    function lowestShare() public view returns (uint32){
        return lowestShareNum;
    }
    //最大认购份额
    function userMaxShare() public view returns (uint){
        return maxShareNum;
    }
    //可认购份额
    function getTotalSubscribedNum() public view returns (uint){
        return totalSubscribedNum;
    }
    //认购开始日期
    function subscribeStartDate() public view returns (uint32){
        return subscriptionStartDate;
    }
    //认购结束日期
    function subscribeEndDate() public view returns (uint32){
        return subscriptionEndDate;
    }

    //锁定期：天
    function getLockupPeriodDays() public view returns (uint16){
        return lockupPeriodDays;
    }

    //查询余额及冻结金额
    function balanceOf(address _owner) public view returns (uint balanceAmount, uint frozenAmount){
        balanceAmount = balance[_owner].balance;
        frozenAmount = getFrozenAmount(_owner);
        return (balanceAmount, frozenAmount);
    }

    //查询余额
    function getBalance(address _owner) internal view returns (uint){
        return balance[_owner].balance;
    }

    //增发
    //num：增发的数量
    function additionalIssue(uint num) public returns (bool){
        require(msg.sender == offerAddress, "msg.sender is not equal to offer address");
        num = num * 10 ** uint(decimalsDigit);
        totalSupplyAmount += num;
        balance[offerAddress].balance += num;
        return true;
    }

    //分页查询持有数量
    //currentPage: 当前页
    //pageSize：每页查询数量
    //返回参数：
    //address[]：地址数组
    //uint[]：余额数组，下标与地址数组应。(两个数组的同一下标对应的值表示：某一个地址对应的余额)
    //uint：表示拥有当前token的总数。
    function pagingQuery(uint currentPage, uint16 pageSize) public view returns (address[], uint[], uint){
        //起始页与查询条数必须大于等于1
        require(currentPage >= 1 && pageSize >= 1);
        if (pageSize > MAX_NUMBER_OF_QUERY_PER_PAGE) {
            pageSize = MAX_NUMBER_OF_QUERY_PER_PAGE;
        }
        uint addressLength = addresses.length;
        if (pageSize > addressLength) {
            pageSize = uint16(addressLength);
        }
        address[] memory addrs;
        uint[] memory emptyBalances;
        //起始索引
        uint start = (currentPage - 1) * pageSize;
        if (start >= addressLength) {
            return (addrs, emptyBalances, addressLength);
        }

        //计算返回数组的初始化长度
        uint16 arrayInitLength = calculateArrayInitLength(currentPage, pageSize, addressLength);
        address emptyAddress;
        addrs = new address[](arrayInitLength);
        emptyBalances = new uint[](arrayInitLength);

        for (uint16 i = 0; i < arrayInitLength; i++) {
            emptyAddress = addresses[start];
            addrs[i] = emptyAddress;
            emptyBalances[i] = balance[emptyAddress].balance;
            start++;
        }

        return (addrs, emptyBalances, addressLength);
    }
    //提取稳定币到发行方账户地址
    // function withDrawal() public returns (bool);

    //转账接口
    //_from：msg.sender
    //_to：接收方地址
    //_value：转账金额
    function transfer(address _to, uint256 _value) public payable returns (bool success){
        //只有在锁定期之后才能进行交易
        require(block.timestamp > subscriptionEndDate + lockupPeriodDays * 1 days, "Trade after a lockup period");
        //接收方校验权限
        require(checkPermission(_to), "to address has no permission");

        success = tokenTransferFrom(msg.sender, _to, _value);
        return success;
    }

    //计算返回的数组初始化长度，避免最后一页初始化长度过长
    function calculateArrayInitLength(uint currentPage, uint16 pageSize, uint arrayLength) private pure returns (uint16){
        if (arrayLength % pageSize == 0) {
            return pageSize;
        }
        uint allPageSize = arrayLength / pageSize + 1;
        if (currentPage <= allPageSize - 1) {
            return pageSize;
        }
        uint16 arrayInitLength = uint16(arrayLength - (currentPage - 1) * pageSize);
        return arrayInitLength;
    }

    /// @param _to address to receive security token
    /// @param _payment stable token payment of subscribe token
    /// @return success whether the contract method invoke is successful
    function receiveToken(address _to, uint256 _payment, uint256 _amount) external returns (bool success){
        require(checkPermission(_to), "to address has no permission");
        require(stableCurrencyContractAddress == msg.sender, "stable address never match");
        require(block.timestamp >= subscriptionStartDate, "not after subscription start date");
        require(block.timestamp <= subscriptionEndDate, "not before subscription end date");
        require(_payment == _amount * exchangeRate, "stable payment not matcht the token amount");
        require(_amount >= lowestShareNum, "amount is smaller than lowestShareNum");
        if (maxShareNum != 0) {
            require(getBalance(_to) + _amount <= maxShareNum, "amount is bigger than lowestShareNum");
        }
        require(_amount <= getBalance(offerAddress) - (getFrozenAmount(offerAddress)), "token balance not enough");
        require(_to != address(0), "address illegal");

        success = tokenTransferFrom(offerAddress, _to, _amount);
        return success;
    }

    ///@return offerAddress the address of contract offer
    function tokenOfferAddress() external view returns (address){
        return offerAddress;
    }

    function tokenTransferFrom(address _from, address _to, uint256 _value) internal returns (bool){
        //转出方必须未被冻结
        require(stoCheck(_from), "from address has been frozen");
        //防止给零地址转账
        require(_to != 0x0, "to address is 0x0");
        //转账金额必须大于0
        require(_value > 0, "The value must be that is greater than zero.");
        //转出方余额必须足够(所有余额减去冻结金额)
        require(balance[_from].balance - getFrozenAmount(_from) >= _value, "from address balance not enough");
        //检查是否溢出
        require(balance[_to].balance + _value > balance[_to].balance, "to address balance overflow");

        //为后面作断言使用
        uint previousBalance = balance[_from].balance + balance[_to].balance;
        //转出方减去转出金额
        balance[_from].balance -= _value;
        if (!balance[_to].exists) {
            //如果接收方不存在balanceOf中，则将转账金额，及地址存入，并添加进地址数组。
            balance[_to].balance = _value;
            balance[_to].exists = true;
            //将地址存入地址数组
            addresses.push(_to);
        }
        else {
            //接收方余额加上转账金额
            balance[_to].balance += _value;
        }
        //通知订阅了此事件的客户端
        emit Transfer(_from, _to, _value);
        //断言转账前后，转出方，接收方余额之和相等。
        assert(balance[_from].balance + balance[_to].balance == previousBalance);

        return true;
    }

    //冻结某个地址上的金额
    //freezeAddr：冻结地址
    //freezeNum: 冻结金额数量
    function freeze(address freezeAddr, uint freezeNum) public returns (bool){
        //只有发行方能操作
        require(msg.sender == offerAddress, "msg.sender is not offer address");
        require(freezeNum > 0, "The value must be that is greater than zero.");
        //余额必须大于等于冻结金额
        require(balance[freezeAddr].balance >= freezeNum, "The balance must be greater than or equal to the amount to be frozen");
        frozenAddressAmount[freezeAddr] += freezeNum;
        return true;
    }

    //sto认购权限添加
    function addAllowedAddr(address[] addr) external returns (bool success){
        require(msg.sender == offerAddress, "msg.sender is not offer address");
        for (uint i = 0; i < addr.length; i++) {
            allowedAddr[addr[i]] = true;
        }
        return true;
    }

    //检查认购权限
    function checkPermission(address addr) public view returns (bool){
        return allowedAddr[addr];
    }
}