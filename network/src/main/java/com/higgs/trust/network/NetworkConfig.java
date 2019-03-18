package com.higgs.trust.network;

import com.higgs.trust.network.message.handler.MessageHandlerRegistry;

import java.util.HashSet;

/**
 * @author duhongming
 * @date 2018/8/30
 */
public class NetworkConfig {
    private String host;
    private int port;
    private int httpPort;
    private String privateKey;
    private String publicKey;
    private String nodeName;
    private HashSet<Address> seeds;
    private Peer localPeer;
    private boolean isBackupNode;
    private boolean singleton;
    private String signature;
    private MessageHandlerRegistry handlerRegistry;
    private int timeout;
    private int clientThreadNum;

    private Authentication authentication;

    private NetworkConfig() {
        seeds = new HashSet();
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public int httpPort() {
        return httpPort;
    }

    public String privateKey() {
        return this.privateKey;
    }

    public String publicKey() {
        return this.publicKey;
    }

    public String nodeName() {
        return this.nodeName;
    }

    public HashSet<Address> seeds() {
        return seeds;
    }

    public Peer localPeer() {
        return localPeer;
    }

    public boolean isBackupNode() {
        return isBackupNode;
    }

    public void setBackupNode(boolean backupNode) {
        isBackupNode = backupNode;
    }

    public Authentication authentication() {
        return authentication;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public String signature() {
        return this.signature;
    }

    public MessageHandlerRegistry handlerRegistry() {
        return this.handlerRegistry;
    }

    public int timeout() {
        return this.timeout;
    }

    /**
     * The num of Netty client EventLoopGroup's threads
     * @return
     */
    public int clientThreadNum() {
        return this.clientThreadNum;
    }

    public static class NetworkConfigBuilder {
        private final NetworkConfig config;

        public NetworkConfigBuilder() {
            config = new NetworkConfig();
        }

        public NetworkConfig build() {
            long nonce = System.currentTimeMillis();
            Address localAddress = new Address(config.host, config.port);
            config.localPeer = new Peer(localAddress, config.nodeName(), config.publicKey());
            config.localPeer.setConnected(true);
            config.localPeer.setNonce(nonce);
            config.localPeer.setHttpPort(config.httpPort);
            config.localPeer.setSlave(config.isBackupNode());

            config.signature = config.authentication.sign(config.localPeer, config.privateKey);

            config.handlerRegistry = new MessageHandlerRegistry();
            return config;
        }

        public NetworkConfigBuilder host(String host) {
            config.host = host;
            return this;
        }

        public NetworkConfigBuilder port(int port) {
            config.port = port;
            return this;
        }

        public NetworkConfigBuilder httpPort(int httpPort) {
            config.httpPort= httpPort;
            return this;
        }

        public NetworkConfigBuilder privateKey(String privateKey) {
            config.privateKey = privateKey;
            return this;
        }

        public NetworkConfigBuilder nodeName(String nodeName) {
            config.nodeName = nodeName;
            return this;
        }

        public NetworkConfigBuilder backupNode(boolean isBackupNode) {
            config.isBackupNode = isBackupNode;
            return this;
        }

        public NetworkConfigBuilder publicKey(String publicKey) {
            config.publicKey = publicKey;
            return this;
        }

        public NetworkConfigBuilder seed(Address... seedPeers) {
            if (seedPeers != null && seedPeers.length > 0) {
                for(Address address : seedPeers) {
                    config.seeds.add(address);
                }
            }
            return this;
        }

        public NetworkConfigBuilder seed(String host, int port) {
            config.seeds.add(new Address(host, port));
            return this;
        }

        public NetworkConfigBuilder seed(int... ports) {
            if (ports.length > 0) {
                for(int port : ports) {
                    config.seeds.add(Address.from(port));
                }
            }
            return this;
        }

        public NetworkConfigBuilder authentication(Authentication authentication) {
            config.authentication = authentication;
            return this;
        }

        public NetworkConfigBuilder singleton() {
            config.singleton = true;
            return this;
        }

        public NetworkConfigBuilder timeout(int timeout) {
            config.timeout = timeout;
            return this;
        }

        public NetworkConfigBuilder clientThreadNum(int num) {
            config.clientThreadNum = num;
            return this;
        }
    }

    public static NetworkConfigBuilder builder() {
        return new NetworkConfigBuilder();
    }
}
