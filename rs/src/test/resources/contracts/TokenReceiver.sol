pragma solidity ^0.4.12;

interface TokenReceiver {
    /// @param _to address to receive security token
    /// @param _payment stable token payment of subscribe token
    /// @param _amount The amount of token to be transferred
    /// @return success whether the contract method invoke is successful
    function receiveToken(address _to, uint256 _payment, uint256 _amount) external returns (bool success);

    ///@return offerAddress the address of contract offer
    function tokenOfferAddress() external view returns (address offerAddress);
}