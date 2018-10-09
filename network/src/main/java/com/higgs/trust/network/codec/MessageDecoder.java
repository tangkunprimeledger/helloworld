package com.higgs.trust.network.codec;

import com.higgs.trust.network.message.NetworkMessage;
import com.higgs.trust.network.message.NetworkRequest;
import com.higgs.trust.network.message.NetworkResponse;
import com.higgs.trust.network.Address;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/8/21
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int BYTE_SIZE = 1;
    private static final int INT_SIZE = 4;
    private static final int LONG_SIZE = 8;

    private int contentLength;
    private String senderIp;
    private int senderPort;
    private long messageId;
    private NetworkMessage.Type messageType;
    private String actionName;
    private byte[] payload;

    private DecoderStatus decoderStatus = DecoderStatus.READ_VERSION;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buffer, List<Object> list) {

        switch (decoderStatus) {
            case READ_VERSION:
                if (buffer.readableBytes() < INT_SIZE) {
                    return;
                }
                log.trace("version: {}", buffer.readShort());
                decoderStatus = DecoderStatus.READ_IP;
            case READ_IP:
                if (buffer.readableBytes() < INT_SIZE) {
                    return;
                }
                buffer.markReaderIndex();
                int ipLen = buffer.readInt();
                if (buffer.readableBytes() < ipLen) {
                    buffer.resetReaderIndex();
                    return;
                }
                byte[] data = new byte[ipLen];
                buffer.readBytes(data);
                senderIp = new String(data);
                log.trace("ip: {}", senderIp);
                decoderStatus = DecoderStatus.READ_PORT;
            case READ_PORT:
                if (buffer.readableBytes() < INT_SIZE) {
                    return;
                }
                senderPort = buffer.readInt();
                log.trace("port: {}", senderPort);
                decoderStatus = DecoderStatus.READ_MESSAGE_ID;
            case READ_MESSAGE_ID:
                if (buffer.readableBytes() < LONG_SIZE) {
                    return;
                }
                messageId = buffer.readLong();
                decoderStatus = DecoderStatus.READ_MESSAGE_TYPE;
            case READ_MESSAGE_TYPE:
                if (buffer.readableBytes() < BYTE_SIZE) {
                    return;
                }
                messageType = NetworkMessage.Type.forId(buffer.readByte());
                if (messageType == NetworkMessage.Type.REQUEST) {
                    decoderStatus = DecoderStatus.READ_REQUEST_ACTION;
                } else {
                    decoderStatus = DecoderStatus.READ_CONTENT_LENGTH;
                }
                return;
            case READ_REQUEST_ACTION:
                if (buffer.readableBytes() < INT_SIZE) {
                    return;
                }
                buffer.markReaderIndex();
                int actionLen = buffer.readInt();
                if (buffer.readableBytes() < actionLen) {
                    buffer.resetReaderIndex();
                    return;
                }

                byte[] actionData = new byte[actionLen];
                buffer.readBytes(actionData);
                actionName = new String(actionData);
                decoderStatus = DecoderStatus.READ_CONTENT_LENGTH;
            case READ_CONTENT_LENGTH:
                if (buffer.readableBytes() < INT_SIZE) {
                    return;
                }
                contentLength = buffer.readInt();
                log.trace("contentLength: {}", contentLength);
                decoderStatus = DecoderStatus.READ_CONTENT;
            case READ_CONTENT:
                if (buffer.readableBytes() < contentLength) {
                    return;
                }

                if (contentLength > 0) {
                    byte[] content = new byte[contentLength];
                    buffer.readBytes(content);
                    payload = content;
                    log.trace("content: {}", new String(content));
                }
                break;
            default:
                break;
        }

        NetworkMessage message = messageType == NetworkMessage.Type.REQUEST
            ? new NetworkRequest(messageId, actionName, payload).sender(new Address(senderIp, senderPort))
            : new NetworkResponse(messageId, NetworkResponse.Status.OK,  payload);

        list.add(message);
        decoderStatus = DecoderStatus.READ_VERSION;
    }

    enum  DecoderStatus {
        READ_VERSION,
        READ_IP,
        READ_PORT,
        READ_MESSAGE_ID,
        READ_MESSAGE_TYPE,
        READ_REQUEST_ACTION,
        READ_CONTENT_LENGTH,
        READ_CONTENT,
    }
}
