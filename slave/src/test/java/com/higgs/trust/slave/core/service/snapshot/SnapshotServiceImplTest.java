package com.higgs.trust.slave.core.service.snapshot;

import com.alibaba.fastjson.JSONArray;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.snapshot.Value;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SnapshotServiceImplTest {

    @Test
    public void testSort() throws Exception {
        List<Pair<Object, Object>> sortingList = new ArrayList<>();
        Value v1= new Value("a", "a", 3);
        Value v2= new Value("b", "b", 1);
        Value v3= new Value("c", "c", 2);
        Value v4= new Value("d", "d", 2);
        Pair<Object, Object> p1 = Pair.of("p1", v1);
        Pair<Object, Object> p2 = Pair.of("p2", v2);
        Pair<Object, Object> p3 = Pair.of("p3", v3);
        Pair<Object, Object> p4 = Pair.of("p4", v4);
        sortingList.add(p1);
        sortingList.add(p2);
        sortingList.add(p3);
        sortingList.add(p4);
        System.out.println("Before sort: "+JSONArray.toJSONString(sortingList));
        sort(sortingList);
        System.out.println("After sort: "+JSONArray.toJSONString(sortingList));
    }

    private void sort( List<Pair<Object, Object>> sortingList){
        //sort list
        Collections.sort(sortingList,  new Comparator<Pair<Object, Object>>() {
            @Override
            public int compare(Pair<Object, Object> p1, Pair<Object, Object> p2) {
                Value value1 = (Value)p1.getRight();
                Value value2 = (Value)p2.getRight();
                if(value1.getIndex() >= value2.getIndex()){
                    return 1;
                }else {
                    return -1;
                }
            }
        });
    }

}