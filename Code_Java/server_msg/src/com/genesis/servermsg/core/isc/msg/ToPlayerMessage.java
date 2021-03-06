package com.genesis.servermsg.core.isc.msg;

import com.genesis.core.net.msg.SCMessage;
import com.genesis.protobuf.MessageType.MessageTarget;
import com.genesis.servermsg.core.msg.IMessage;

/**
 * 需要转发给玩家客户端的消息
 *
 * @author yaguang.xiao
 *
 */

public class ToPlayerMessage implements IMessage {

    /** 玩家账号Id */
    public final long agentSessionId;
    /** 需要转发给玩家客户端的消息 */
    public final SCMessage msg;

    public ToPlayerMessage(long agentSessionId, SCMessage msg) {
        this.agentSessionId = agentSessionId;
        this.msg = msg;
    }

    @Override
    public MessageTarget getTarget() {
        return MessageTarget.ISC_ACTOR;
    }
}
