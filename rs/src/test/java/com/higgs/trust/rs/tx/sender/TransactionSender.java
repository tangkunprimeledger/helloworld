package com.higgs.trust.rs.tx.sender;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.rs.core.bo.ContractQueryRequestV2;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
@Slf4j
public class TransactionSender {
    @AllArgsConstructor
    private static class Entity {
        private String serverIp;
        private int serverPort;
        private ITransactionSender transactionSender;
        private String nodeName;
        private String consensusPrivateKey;
    }

    public static final List<Entity> ENTITIES = new ArrayList<>();
    public static final String SENDER_ADDRESS;
    private static final File file = Paths.get("rs/src/test/resources/tmp/deployedContracts.dat").toFile();
    private static final Properties properties = new Properties();
    private static final List<String> DEPLOYED_CONTRACTS = Collections.synchronizedList(new ArrayList<>());

    static {
        HttpClient httpClient0 = new HttpClient("127.0.0.1", 7070);
        ITransactionSender poster0 = httpClient0.createApi(ITransactionSender.class);
        ENTITIES.add(new Entity("127.0.0.1", 7070, poster0, "TRUST-TEST0", "78d4646b8baa8cfbe4c02b9d245e4ee2406af092955d705d91e83238e012b707"));
        HttpClient httpClient1 = new HttpClient("127.0.0.1", 7071);
        ITransactionSender poster1 = httpClient1.createApi(ITransactionSender.class);
        ENTITIES.add(new Entity("127.0.0.1", 7071, poster1, "TRUST-TEST1", "cec12d357d0f72add5daa0b632404b37eed7af843d1d6eba187cfd7a39fc9030"));
        HttpClient httpClient2 = new HttpClient("127.0.0.1", 7072);
        ITransactionSender poster2 = httpClient2.createApi(ITransactionSender.class);
        ENTITIES.add(new Entity("127.0.0.1", 7072, poster2, "TRUST-TEST2", "888cb7c1fd97df6b6bcc147ddc35119869a162089b74f4d975208273c1d75c7e"));
        HttpClient httpClient3 = new HttpClient("127.0.0.1", 7073);
        ITransactionSender poster3 = httpClient3.createApi(ITransactionSender.class);
        ENTITIES.add(new Entity("127.0.0.1", 7073, poster3, "TRUST-TEST3", "d9027c2e08b6d224615b1f61c3ec5080c2c45c914d947b7b1f86c517bf3eeec6"));

        SENDER_ADDRESS = "44140ed117f968181823ca021394152800b51214";
    }

    private static Entity getRandomEntity() {
        SecureRandom random = new SecureRandom();
        return ENTITIES.get(random.nextInt(ENTITIES.size()));
    }

    private static String getRandomDeployedContract() {
        SecureRandom random = new SecureRandom();
        return DEPLOYED_CONTRACTS.get(random.nextInt(DEPLOYED_CONTRACTS.size()));
    }

    public static void main(String[] args) throws IOException {
        loadDeployedContracts();
        Entity entity = getRandomEntity();
        SignedTransaction signedTransaction = TransactionBuilder.generateSignedTransactionWithContractCreation(
                SENDER_ADDRESS,
                entity.nodeName,
                Paths.get("rs/src/test/resources/contracts/DataWrapper.sol").toFile().getAbsolutePath(),
                "DataWrapper",
                "DataWrapper()",
                entity.consensusPrivateKey,
                new Object[0]);
        log.info(JSON.toJSONString(signedTransaction, true));
        RespData respData = entity.transactionSender.post(signedTransaction).execute().body();
        if (respData.getData() != null) {
            addDeployedContract(((ContractCreationV2Action) signedTransaction.getCoreTx().getActionList().get(0)).getTo());
        }
        log.info(respData.toString());
        storeDeployedContracts();


        for (int i = 0; i < 200; i++) {
            Entity entity1 = getRandomEntity();
            SignedTransaction signedTransaction1 = TransactionBuilder.generateSignedTransactionWithContractInvocation(
                    SENDER_ADDRESS,
                    getRandomDeployedContract(),
                    entity1.nodeName,
                    "() addOne()",
                    entity1.consensusPrivateKey,
                    new Object[0]);
            log.info(JSON.toJSONString(signedTransaction1, true));
            RespData respData1 = entity1.transactionSender.post(signedTransaction1).execute().body();
            log.info(respData1.toString());
        }


        Entity entity2 = getRandomEntity();
        ContractQueryRequestV2 contractQueryRequestV2 = TransactionBuilder.generateContractQueryRequestV2(
                -1L,
                "(uint256) get()",
                new Object[0],
                getRandomDeployedContract());
        RespData respData2 = entity2.transactionSender.post(contractQueryRequestV2).execute().body();
        System.out.println(respData2);
    }


    private static void storeDeployedContracts() {
        try {
            OutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "No comments");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addDeployedContract(String contractAddress) {
        if (!DEPLOYED_CONTRACTS.contains(contractAddress)) {
            DEPLOYED_CONTRACTS.add(contractAddress);
            properties.setProperty(contractAddress, contractAddress);
        }
    }

    private static void loadDeployedContracts() {
        try {
            InputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            DEPLOYED_CONTRACTS.addAll(Arrays.asList(properties.keySet().toArray(new String[0])));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
