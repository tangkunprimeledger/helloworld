package com.higgs.trust.evmcontract.db;

import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.datasource.inmem.HashMapDB;
import com.higgs.trust.evmcontract.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author tangkun
 * @date 2018-11-14
 */
public class RepositoryRootTest {

    @Test
    public void test11() {

        RepositoryRoot repository = new RepositoryRoot(new HashMapDB());
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        byte[] cowCode = Hex.decode("1111111111");
        byte[] horseCode = Hex.decode("2222222222");

        track.saveCode(cow, cowCode);
        track.saveCode(horse, horseCode);

        assertArrayEquals(cowCode, track.getCode(cow));
        assertArrayEquals(horseCode, track.getCode(horse));

        DataWord key = new DataWord("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        track.addStorageRow(cow,key,new DataWord("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        track.addStorageRow(cow,new DataWord("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"),new DataWord("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"));

        track.addStorageRow(horse,new DataWord("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),new DataWord("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        track.addStorageRow(horse,new DataWord("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"),new DataWord("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"));

        track.getStorageValue(cow,key);

        track.commit();
        repository.commit();

        assertArrayEquals(cowCode, repository.getCode(cow));
        assertArrayEquals(horseCode, repository.getCode(horse));

        repository.close();
    }

}