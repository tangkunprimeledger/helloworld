package com.higgs.trust.config.view;

import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * @author Zhu_Yuanxiang
 * @create 2018-09-21
 */
public class ClusterViewTest extends PowerMockTestCase {

    @Test
    public void testGetAppliedQuorum(){
        ClusterView view;
        //no any node
        view = getClusterViewWithNodeNum(0);
        Assert.assertEquals(view.getAppliedQuorum(),0);
        //1 node
        view = getClusterViewWithNodeNum(1);
        Assert.assertEquals(view.getAppliedQuorum(),1);
        //2 nodes
        view = getClusterViewWithNodeNum(2);
        Assert.assertEquals(view.getAppliedQuorum(),2);
        //3 nodes
        view = getClusterViewWithNodeNum(3);
        Assert.assertEquals(view.getAppliedQuorum(),2);
        //4 nodes
        view = getClusterViewWithNodeNum(4);
        Assert.assertEquals(view.getAppliedQuorum(),3);
        //5 nodes
        view = getClusterViewWithNodeNum(5);
        Assert.assertEquals(view.getAppliedQuorum(),4);
        //10 nodes
        view = getClusterViewWithNodeNum(10);
        Assert.assertEquals(view.getAppliedQuorum(),7);
        //n nodes
        for(int i=1;i<1000;i++){
            view = getClusterViewWithNodeNum(i);
            //要求任意两个AppliedQuorum集合应取“最小的”“交集大于f”的集合
            Assert.assertTrue(2*view.getAppliedQuorum()-i>view.getFaultNum()
                    && 2*(view.getAppliedQuorum()-1)-i<=view.getFaultNum());
        }
    }

    @Test
    public void testGetVerifiedQuorum(){
        ClusterView view;
        //no any node
        view = getClusterViewWithNodeNum(0);
        Assert.assertEquals(view.getVerifiedQuorum(),0);
        //1 node
        view = getClusterViewWithNodeNum(1);
        Assert.assertEquals(view.getVerifiedQuorum(),1);
        //2 nodes
        view = getClusterViewWithNodeNum(2);
        Assert.assertEquals(view.getVerifiedQuorum(),1);
        //3 nodes
        view = getClusterViewWithNodeNum(3);
        Assert.assertEquals(view.getVerifiedQuorum(),1);
        //4 nodes
        view = getClusterViewWithNodeNum(4);
        Assert.assertEquals(view.getVerifiedQuorum(),2);
        //5 nodes
        view = getClusterViewWithNodeNum(5);
        Assert.assertEquals(view.getVerifiedQuorum(),2);
        //10 nodes
        view = getClusterViewWithNodeNum(10);
        Assert.assertEquals(view.getVerifiedQuorum(),4);
        //n nodes
        for(int i=1;i<1000;i++){
            view = getClusterViewWithNodeNum(i);
            //要求VerifiedQuorum集合应取“最小的”“大于f”的集合
            Assert.assertTrue(view.getVerifiedQuorum()>view.getFaultNum()
                    &&view.getVerifiedQuorum()-1<=view.getFaultNum());
        }
    }

    private ClusterView getClusterViewWithNodeNum(int num){
        HashMap<String, String> nodes = new HashMap<>();
        int nodeID=0;
        for(;nodeID<num;nodeID++) {
            nodes.put("node" + nodeID, "publicKey"+nodeID);
        }
        return new ClusterView(1,1,nodes);
    }
}
