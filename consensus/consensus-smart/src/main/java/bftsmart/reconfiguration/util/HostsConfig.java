/**
 * Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bftsmart.reconfiguration.util;

import com.higgs.trust.consensus.bftsmartcustom.started.custom.SpringUtil;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class HostsConfig {

    private com.higgs.trust.consensus.bftsmartcustom.started.custom.config.SmartConfig config;

    private Hashtable servers = new Hashtable();

    public HostsConfig() {
    }

    /**
     * Creates a new instance of ServersConfig
     */
    public HostsConfig(String configHome, String fileName) {
        config = SpringUtil.getBean(com.higgs.trust.consensus.bftsmartcustom.started.custom.config.SmartConfig.class);
        loadConfig(configHome, fileName);
    }

    private void loadConfig(String configHome, String fileName) {
        String hostsConfigs = config.getHostsConfig();
        if (!StringUtils.isEmpty(hostsConfigs)) {
            String[] configs = hostsConfigs.split(",", -1);
            if (configs != null && configs.length > 0) {
                for (String item : configs) {
                    StringTokenizer stringTokenizer = new StringTokenizer(item.trim(), " ");
                    if (stringTokenizer.countTokens() > 2) {
                        int id = Integer.valueOf(stringTokenizer.nextToken());
                        String host = stringTokenizer.nextToken();
                        int port = Integer.valueOf(stringTokenizer.nextToken());
                        this.servers.put(id, new Config(id, host, port));
                    }
                }
            }
        }
        //        try{
        //            String path =  "";
        //            String sep = System.getProperty("file.separator");
        //            if(configHome.equals("")){
        //                   if (fileName.equals(""))
        //                        path = "config"+sep+"hosts.config";
        //                   else
        //                        path = "config"+sep+fileName;
        //            }else{
        //                   if (fileName.equals(""))
        //                        path = configHome+sep+"hosts.config";
        //                   else
        //                       path = configHome+sep+fileName;
        //            }
        //            FileReader fr = new FileReader(path);
        //            BufferedReader rd = new BufferedReader(fr);
        //            String line = null;
        //            while((line = rd.readLine()) != null){
        //                if(!line.startsWith("#")){
        //                    StringTokenizer str = new StringTokenizer(line," ");
        //                    if(str.countTokens() > 2){
        //                        int id = Integer.valueOf(str.nextToken());
        //                        String host = str.nextToken();
        //                        int port = Integer.valueOf(str.nextToken());
        //                        this.servers.put(id, new Config(id,host,port));
        //                    }
        //                }
        //            }
        //            fr.close();
        //            rd.close();
        //        }catch(Exception e){
        //            Logger.printError(e.getMessage(),e);
        //        }
    }

    public void add(int id, String host, int port) {
        if (this.servers.get(id) == null) {
            this.servers.put(id, new Config(id, host, port));
        }
    }

    public int getNum() {
        return servers.size();
    }

    public InetSocketAddress getRemoteAddress(int id) {
        Config c = (Config)this.servers.get(id);
        if (c != null) {
            return new InetSocketAddress(c.host, c.port);
        }
        return null;
    }

    public InetSocketAddress getServerToServerRemoteAddress(int id) {
        Config c = (Config)this.servers.get(id);
        if (c != null) {
            return new InetSocketAddress(c.host, c.port + 1);
        }
        return null;
    }

    public int getPort(int id) {
        Config c = (Config)this.servers.get(id);
        if (c != null) {
            return c.port;
        }
        return -1;
    }

    public int getServerToServerPort(int id) {
        Config c = (Config)this.servers.get(id);
        if (c != null) {
            return c.port + 1;
        }
        return -1;
    }

    public int[] getHostsIds() {
        Set s = this.servers.keySet();
        int[] ret = new int[s.size()];
        Iterator it = s.iterator();
        int p = 0;
        while (it.hasNext()) {
            ret[p] = Integer.parseInt(it.next().toString());
            p++;
        }
        return ret;
    }

    public void setPort(int id, int port) {
        Config c = (Config)this.servers.get(id);
        if (c != null) {
            c.port = port;
        }
    }

    public String getHost(int id) {
        Config c = (Config)this.servers.get(id);
        if (c != null) {
            return c.host;
        }
        return null;
    }

    public InetSocketAddress getLocalAddress(int id) {
        Config c = (Config)this.servers.get(id);
        if (c != null) {
            return new InetSocketAddress(c.port);
        }
        return null;
    }

    public class Config {
        public int id;
        public String host;
        public int port;

        public Config(int id, String host, int port) {
            this.id = id;
            this.host = host;
            this.port = port;
        }
    }
}
