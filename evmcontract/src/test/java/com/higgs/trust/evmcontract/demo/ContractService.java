package com.higgs.trust.evmcontract.demo;

import com.higgs.trust.evmcontract.config.SystemProperties;
import com.higgs.trust.evmcontract.core.Block;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.core.Transaction;
import com.higgs.trust.evmcontract.datasource.DbSource;
import com.higgs.trust.evmcontract.datasource.rocksdb.RocksDbDataSource;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.db.RepositoryRoot;
import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.vm.VM;
import com.higgs.trust.evmcontract.vm.program.Program;
import com.higgs.trust.evmcontract.vm.program.ProgramResult;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvoke;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvokeImpl;
import org.apache.commons.io.IOUtils;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * @author duhongming
 * @date 2018/11/15
 */
public class ContractService {

    private SystemProperties config;
    private DbSource<byte[]> db;
    private RepositoryRoot repository;

    //private boolean initialized;

    private final  static byte[] gasPrice = Hex.decode("e8d4a51000");  // 1000000000000
    private final  static byte[] gas = Hex.decode("27100000000000");            // 10000

    public ContractService(byte[] rootHash) {
        this.config = SystemProperties.getDefault();
        this.db = new RocksDbDataSource("trust");
//        this.db = new HashMapDB<>();
        db.init();
        if (rootHash == null || rootHash.length == 0) {
            rootHash = db.get("RootHash".getBytes());
        }

        if (rootHash != null) {
            System.out.println("Get RootHash:" + Hex.toHexString(rootHash));
        }

        this.repository = new RepositoryRoot(db, rootHash);

//        this.repository.addBalance(Hex.decode("13434c369b163A16F969C15F965618a724b3a634"), new BigInteger("10000000"));
//        BigInteger balance = this.repository.getBalance(Hex.decode("13434c369b163A16F969C15F965618a724b3a634"));
//        System.out.println("balance: " + balance);
    }

    public static void main(String[] args) {
        byte[] root = Hex.decode("");
        ContractService contractService = new ContractService(root);
        byte[] contractAddress = contractService.createContract();
        contractService.commit();

        contractService.invoke(contractAddress);
        contractService.commit();
    }

    public void destroy() {
        repository.close();
        db.close();
    }

    public void commit() {
        this.repository.commit();
//        String dumpStr = this.repository.dumpStateTrie();
//        System.out.println(dumpStr);
        System.out.printf("RootHash=%s\n", Hex.toHexString(repository.getRoot()));
        db.put("RootHash".getBytes(), repository.getRoot());
    }

    public byte[] createContract() {
        byte[] nonce = BigIntegers.asUnsignedByteArray(BigInteger.ZERO);
     //   byte[] code = loadCodeFromResourceFile("D:\\tmp\\demo.txt");
        byte[] receiveAddress = Hex.decode("");//13978aee95f38490e9769c39b2773ed763d9cd5f
        byte[] value = Hex.decode("");          //10000000000000000 2386f26fc10000"
     byte[]   code = Hex.decode("6060604052341561000f57600080fd5b5b61025c8061001f6000396000f3006" +
                "0606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063e0041" +
                "3961461003e575b600080fd5b341561004957600080fd5b6100ab600480803590602001908201803590602001908080601f016" +
                "02080910402602001604051908101604052809392919081815260200183838082843782019150505050505091908035906020019091908" +
                "035906020019091905050610127565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101" +
                "56100ec5780820151818401525b6020810190506100d0565b50505050905090810190601f1680156101195780820380516001836020036101000a" +
                "031916815260200191505b509250505060405180910390f35b61012f61021c565b61013761021c565b6000836040518059106101475750595b90808" +
                "2528060200260200182016040525b509150600090505b8381101561020f578585820181518110151561017857fe5b9060200101517f010000000000" +
                "000000000000000000000000000000000000000000000000000090047f0100000000000000000000000000000000000000000000000000000000000000" +
                "0282828151811015156101d157fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90" +
                "53505b8080600101915050610160565b8192505b50509392505050565b6020604051908101604052806000815250905600a165627a7a72305820f953" +
                "c788cc60245aaada7955797472a879c5755c1bb7c9fe47ce896a9ce3e9a60029");
        byte[] address = createContract(nonce, receiveAddress, value, code);
        System.out.printf("address=%s\n", Hex.toHexString(address));
        System.out.printf("RootHash=%s\n", Hex.toHexString(repository.getRoot()));
        return address;
    }

    public void invoke(byte[] contractAddress) {
        byte[] value = Hex.decode(""); //10000000000000000 2386f26fc10000"
        byte[] nonce = BigIntegers.asUnsignedByteArray(BigInteger.ZERO);

        //call contract
        byte[] data = Hex.decode("e0041396\n" +
                "0000000000000000000000000000000000000000000000000000000000000060\n" +
                "0000000000000000000000000000000000000000000000000000000000000002\n" +
                "0000000000000000000000000000000000000000000000000000000000000003\n" +
                "000000000000000000000000000000000000000000000000000000000000000eabababababababababababababab000000000000000000000000000000000000");
        System.out.println("thread id:" + Thread.currentThread().getName());
        invokeContract(nonce, contractAddress, value, data);
    }


    public byte[] createContract(byte[] nonce, byte[] receiveAddress, byte[] value, byte[] data) {
        Transaction tx = new Transaction(nonce, gasPrice, gas, receiveAddress, value, data);

        Block block = new Block();
        Repository txTrack = repository.startTracking();
        Repository contractTrack = txTrack.startTracking();

        ProgramInvoke programInvoke = createProgramInvoke(tx, block, contractTrack, null);
        Program program = new Program(tx.getData(), programInvoke, tx, SystemProperties.getDefault());

        VM vm = new VM(config);
        vm.play(program);

        ProgramResult result = program.getResult();
        if (!result.isRevert()) {
            contractTrack.saveCode(tx.getContractAddress(), result.getHReturn());
        }
        contractTrack.commit();
        txTrack.commit();
//        repository.commit();

//        System.out.println("合約result:" + Hex.toHexString(program.getResult().getHReturn()));
//        byte[] root = repository.getRoot();
//        System.out.println("RootHash:" + Hex.toHexString(root));
        return tx.getContractAddress();
    }

    public byte[] invokeContract(byte[] nonce, byte[] contractAddress, byte[] value, byte[] data) {
        Transaction tx = new Transaction(nonce, gasPrice, gas, contractAddress, value, data);

        Block block = new Block();
        Repository txTrack = repository.startTracking();
        Repository contractTrack = txTrack.startTracking();

        ProgramInvoke programInvoke = createProgramInvoke(tx, block, contractTrack, null);
        Program  program = new Program(contractTrack.getCode(contractAddress), programInvoke, tx, SystemProperties.getDefault());

        VM vm = new VM(config);
        vm.play(program);

        ProgramResult result = program.getResult();
        contractTrack.commit();
        txTrack.commit();
//        repository.commit();
        System.out.println("合約result:" + Hex.toHexString(result.getHReturn()));
//        byte[] root = repository.getRoot();
//        System.out.println("RootHash:" + Hex.toHexString(root));
        return result.getHReturn();
    }


    public ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository,
                                             BlockStore blockStore) {
        /***         ADDRESS op       ***/
        // YP: Get address of currently executing account.
        byte[] address = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();

        /***         ORIGIN op       ***/
        // YP: This is the sender of original transaction; it is never a contract.
        byte[] origin = tx.getSender();

        /***         CALLER op       ***/
        // YP: This is the address of the account that is directly responsible for this execution.
        byte[] caller = tx.getSender();

        /***         BALANCE op       ***/
        byte[] balance = repository.getBalance(address).toByteArray();

        /***         GASPRICE op       ***/
        byte[] gasPrice = tx.getGasPrice();

        /*** GAS op ***/
        byte[] gas = tx.getGasLimit();

        /***        CALLVALUE op      ***/
        byte[] callValue = nullToEmpty(tx.getValue());

        /***     CALLDATALOAD  op   ***/
        /***     CALLDATACOPY  op   ***/
        /***     CALLDATASIZE  op   ***/
        byte[] data = tx.isContractCreation() ? ByteUtil.EMPTY_BYTE_ARRAY : nullToEmpty(tx.getData());

        /***    PREVHASH  op  ***/
        byte[] lastHash = block.getParentHash();

        /***   COINBASE  op ***/
        byte[] coinbase = block.getCoinbase();

        /*** TIMESTAMP  op  ***/
        long timestamp = block.getTimestamp();

        /*** NUMBER  op  ***/
        long number = block.getNumber();

        /*** DIFFICULTY  op  ***/
        byte[] difficulty = block.getDifficulty();

        /*** GASLIMIT op ***/
        byte[] gaslimit = block.getGasLimit();


        return new ProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue, data,
                lastHash, coinbase, timestamp, number, difficulty, gaslimit,
                repository, blockStore);
    }

    public static byte[] loadCodeFromResourceFile(String filePath) {
        try {
            String code = IOUtils.toString(new File(filePath).toURI(), "UTF-8");
            return Hex.decode(code);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
