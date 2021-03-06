package com.genesis.servermsg.core.msg;

import com.genesis.protobuf.MessageType;
import com.genesis.protobuf.MessageType.CGMessageType;
import com.genesis.protobuf.MessageType.MessageTarget;

/**
 * 经由网关转发的客户端消息
 *
 * @author yaguang.xiao
 *
 */

public class CSSMessage implements IMessage {

    public long agentSessionId;
    public int messageType;
    public byte[] messageContent;

    public CSSMessage() {

    }

    public CSSMessage(long agentSessionId, int messageType, byte[] messageContent) {
        this.agentSessionId = agentSessionId;
        this.messageType = messageType;
        this.messageContent = messageContent;
    }

    public long getAgentSessionId() {
        return agentSessionId;
    }

    public void setAgentSessionId(long agentSessionId) {
        this.agentSessionId = agentSessionId;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    @Override
    public MessageTarget getTarget() {
        return CGMessageType.valueOf(messageType).getValueDescriptor().getOptions()
                .getExtension(MessageType.tARGET);
    }

}
