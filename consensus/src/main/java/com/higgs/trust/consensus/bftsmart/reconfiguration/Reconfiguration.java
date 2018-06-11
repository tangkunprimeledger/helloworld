/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.higgs.trust.consensus.bftsmart.reconfiguration;

import com.higgs.trust.consensus.bftsmart.tom.ServiceProxy;
import com.higgs.trust.consensus.bftsmart.tom.core.messages.TOMMessageType;
import com.higgs.trust.consensus.bftsmart.tom.util.TOMUtil;

/**
 *
 * @author eduardo
 */
public class Reconfiguration {

    private ReconfigureRequest request;
    private ServiceProxy proxy;
    private int id;
    
    public Reconfiguration(int id) {
        this.id = id;
         //proxy = new ServiceProxy(id);
        //request = new ReconfigureRequest(id);
    }
    
    public void connect(){
        if(proxy == null){
            proxy = new ServiceProxy(id);
        }
    }
    
    public void addServer(int id, String ip, int port, byte[] sign){
        this.setReconfiguration(ServerViewController.ADD_SERVER, id + ":" + ip + ":" + port, sign);
    }
    
    public void removeServer(int id, byte[] sign){
        this.setReconfiguration(ServerViewController.REMOVE_SERVER, String.valueOf(id), sign);
    }
    

//    public void setF(int f, byte[] sign, String nodeName){
//      this.setReconfiguration(ServerViewController.CHANGE_F,String.valueOf(f), sign, nodeName);
//    }
    
    
    public void setReconfiguration(int prop, String value, byte[] sign){
        if(request == null){
            //request = new ReconfigureRequest(proxy.getViewManager().getStaticConf().getProcessId());
            request = new ReconfigureRequest(id);
        }
        request.setNumber(Integer.valueOf(value));
        request.setOtherSignature(sign);
        request.setProperty(prop, value);
    }
    
    public ReconfigureReply execute(){
        byte[] signature = TOMUtil.signMessage(proxy.getViewManager().getStaticConf().getRSAPrivateKey(),
                                                                            request.toString().getBytes());
        request.setSignature(signature);
        byte[] reply = proxy.invoke(TOMUtil.getBytes(request), TOMMessageType.RECONFIG);
        request = null;
        return (ReconfigureReply) TOMUtil.getObject(reply);
    }
    
    
    public void close(){
        proxy.close();
        proxy = null;
    }
    
}
