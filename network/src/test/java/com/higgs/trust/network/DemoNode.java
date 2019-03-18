package com.higgs.trust.network;

import java.util.Scanner;

public class DemoNode {

    public static void main(String[] args) {
        String nodeName = args[0];
        int port = Integer.parseInt(args[1]);
        String publicKey = args[2];
        NetworkConfig networkConfig = NetworkConfig.builder()
                .host("127.0.0.1")
                .port(port)
                .nodeName(nodeName)
                .publicKey(publicKey)
                .seed("127.0.0.1", 9001)
                .seed("127.0.0.1", 9002)
                .authentication(new AuthenticationImp())
                .build();
        NetworkManage networkManage = new NetworkManage(networkConfig);

        initHandler(networkManage);
        networkManage.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String cmd = scanner.nextLine();
            switch (cmd) {
                case "mm":
//                    BlockHeader blockHeader = new BlockHeader();
//                    blockHeader.setBlockHash("xxxxxxxxxxxxxxxxx");
//                    blockHeader.setBlockTime(new Date().getTime());
//                    blockHeader.setHeight(1L);
//                    blockHeader.setPreviousHash("0000000000000000");
//                    blockHeader.setVersion("v1");
//                    BlockHeaderCmd blockHeaderCmd = new BlockHeaderCmd(blockHeader, 1L);
//
//                    ValidCommandWrap validCommandWrap = new ValidCommandWrap();
//                    validCommandWrap.setValidCommand(blockHeaderCmd);
//                    validCommandWrap.setSign("kdddddddddddddddddddddddddddddddd");
//                    networkManage.broadcast("receive_command", validCommandWrap);
                    break;
                case "print": {
                    networkManage.getPeers().forEach(System.out::println);
                    break;
                }
                case "connected": {
                    networkManage.connectedPeers.forEach(System.out::println);
                    break;
                }
                case "unconnected": {
                    networkManage.unconnectedPeers.forEach(System.out::println);
                }
            }
        }
    }

    static void initHandler(NetworkManage networkManage) {
        //networkManage.messagingService.registerHandler("receive_command", receiveCommandHandler);
    }

//    private static BiFunction<Address, byte[], CompletableFuture<byte[]>> receiveCommandHandler = (address, bytes) -> {
//        ValidCommandWrap request = (ValidCommandWrap) Hessian.parse(bytes);
//        System.out.println(JsonUtils.serialize(request, true));
//        CompletableFuture<byte[]> future = new CompletableFuture<>();
//        System.out.println(String.format("接收到来自%s的FindPeerRequest请求 ...", address));
//        new Thread(() -> future.complete(new byte[]{0b0})).start();
//        return future;
//    };
}
