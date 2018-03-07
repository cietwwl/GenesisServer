package com.genesis.gateserver.funcs;

import com.genesis.gateserver.core.session.AgentClientSessions;
import com.mokylin.bleach.core.isc.remote.IRemote;
import com.mokylin.bleach.core.msgfunc.MsgArgs;
import com.mokylin.bleach.core.msgfunc.server.IServerMsgFunc;
import com.genesis.protobuf.MessageType.MessageTarget;
import com.mokylin.bleach.servermsg.agentserver.RemovePlayerMsg;
import com.genesis.network2client.session.IClientSession;

public class RemovePlayerMsgFunc implements IServerMsgFunc<RemovePlayerMsg, MsgArgs, MsgArgs> {

    @Override
    public void handle(IRemote remote, RemovePlayerMsg msg, MsgArgs arg1, MsgArgs arg2) {
        IClientSession session = AgentClientSessions.Inst.get(msg.agentSessionId);
        if (session != null) {
            session.disconnect();
        }
    }

    @Override
    public MessageTarget getTarget() {
        return MessageTarget.ISC_ACTOR;
    }

}
