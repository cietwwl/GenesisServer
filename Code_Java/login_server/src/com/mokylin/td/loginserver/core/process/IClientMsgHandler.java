package com.mokylin.td.loginserver.core.process;

import com.google.protobuf.GeneratedMessage;
import com.mokylin.td.network2client.core.session.IClientSession;

public interface IClientMsgHandler<Msg extends GeneratedMessage> {

    void handle(IClientSession session, Msg msg);
}
