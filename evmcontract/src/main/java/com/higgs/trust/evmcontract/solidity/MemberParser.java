package com.higgs.trust.evmcontract.solidity;

/**
 * Parses the specified member of a solidity contract. The
 * member can be given with a value of the generic type
 * {@code I}, and result of parsing can be a instance of the
 * generic type  {@code O}, an event, a constructor, or a
 * function.
 *
 * @author Chen Jiawei
 * @date 2018-12-29
 */
public interface MemberParser<I, O extends Abi.Entry> {
    /**
     * Parses the specified member of a solidity contract,
     * according to the input information.
     *
     * @param input input information for parsing
     * @return result of parsing
     */
    O parse(I input);
}
