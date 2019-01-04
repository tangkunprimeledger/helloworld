package com.higgs.trust.rs.tx.multinodes;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
public class TransactionSender {
    public static final String SENDER_ADDRESS = "44140ed117f968181823ca021394152800b51214";
    public static final List<ISignedTransactionPoster> POSTERS = new ArrayList<>();
    public static final List<String> CONTRACT_ADDRESSES = Collections.synchronizedList(new ArrayList<>());
    private static final File file = Paths.get("rs/src/test/resources/tmp/contractAddress.dat").toFile();
    private static Properties properties = new Properties();

    static {
        HttpClient httpClient0 = new HttpClient("127.0.0.1", 7070);
        ISignedTransactionPoster poster0 = httpClient0.createApi(ISignedTransactionPoster.class);
        HttpClient httpClient1 = new HttpClient("127.0.0.1", 7071);
        ISignedTransactionPoster poster1 = httpClient1.createApi(ISignedTransactionPoster.class);
        HttpClient httpClient2 = new HttpClient("127.0.0.1", 7072);
        ISignedTransactionPoster poster2 = httpClient2.createApi(ISignedTransactionPoster.class);
        HttpClient httpClient3 = new HttpClient("127.0.0.1", 7073);
        ISignedTransactionPoster poster3 = httpClient3.createApi(ISignedTransactionPoster.class);

        POSTERS.add(poster0);
        POSTERS.add(poster1);
        POSTERS.add(poster2);
        POSTERS.add(poster3);

        try {
            InputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            CONTRACT_ADDRESSES.addAll(Arrays.asList(properties.keySet().toArray(new String[0])));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setContractAddress(String contractAddress) {
        try {
            InputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            if (!properties.contains(contractAddress)) {
                properties.setProperty(contractAddress, contractAddress);
            }
            OutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "No comments");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
