package com.mokylin.td.network2client.core.handle;

import com.google.protobuf.GeneratedMessage;
import com.mokylin.bleach.core.net.msg.CSMessage;
import com.mokylin.bleach.core.net.msg.SCMessage;
import com.mokylin.bleach.core.time.TimeService;
import com.genesis.protobuf.MessageType;
import com.mokylin.td.network2client.core.channel.IChannelListener;
import com.mokylin.td.network2client.core.id.IdGenerator;
import com.mokylin.td.network2client.core.session.IClientSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

/**
 * 服务器响应客户端IO消息的Handler，用于netty。<p>
 *
 * 该handler主要响应的事件：<br>
 * <li>channelActive: 连接建立，增加ctx个数，将对应的ChannelHandlerContext包装成ISession</li>
 * <li>channelInactive: 连接关闭，减少ctx个数，移除对应的上下文ISession</li>
 * <li>channelRead: 收取消息，将合法的消息交由INettyMessageHandler处理</li>
 * <li>exceptionCaught: 捕获异常，记录日志，断开客户端连接</li>
 * <br>
 *
 * 该handler在创建的时候，可以使用默认提供的MessageProcess作为消息处理器，也可以根据需要指定自己的实现。
 *
 * @see IClientSession
 * @see INettyMessageHandler
 *
 * @author pangchong
 *
 */

@Sharable
public class ServerIoHandler extends ChannelInboundHandlerAdapter {

    /** 会话对应的键 */
    public final static AttributeKey<IClientSession> sessionKey =
            AttributeKey.valueOf("SessionKey");
    /** 日志 */
    private final static Logger log = LoggerFactory.getLogger(ServerIoHandler.class);
    /** 网络消息处理器 */
    private final INettyMessageHandler msgProcess;
    /** 网络通道监听器 */
    private final IChannelListener channelListener;
    /** 连接的个数 */
    private AtomicInteger ctxCount = new AtomicInteger(0);

    public ServerIoHandler(INettyMessageHandler msgProcess, IChannelListener channelListener) {
        if (msgProcess == null) {
            throw new IllegalArgumentException("Message Process for IO Handler can not be null!");
        }
        this.msgProcess = msgProcess;
        if (channelListener == null) {
            throw new IllegalArgumentException("Record Session for IO Handler can not be null!");
        }
        this.channelListener = channelListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctxCount.incrementAndGet();
        log.info("Session opened: {}", ctx.channel().localAddress());
        IClientSession session = new GameSession(ctx);
        this.channelListener.onChannelActive(session);
        ctx.attr(sessionKey).set(session);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctxCount.decrementAndGet();
        this.channelListener.onChannelInActive(ctx.attr(sessionKey).get());
        ctx.attr(sessionKey).remove();
        log.info("Session closed: {}", ctx.channel().localAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof CSMessage)) {
            log.warn("Something received strange: {}", msg);
            return;
        }

        CSMessage csMsg = (CSMessage) msg;
        IClientSession session = ctx.attr(sessionKey).get();
        if (log.isDebugEnabled()) {
            log.debug("[[[Received message]]]: session - {}, code - {}, type - {}",
                    session.getSessionId(), csMsg.messageType,
                    MessageType.CGMessageType.valueOf(csMsg.messageType));
        }
        msgProcess.handle(session, (CSMessage) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

    /**
     * ChannelHandlerContext包装类，实现了ISession，为之后的网络操作提供操作。
     *
     * @see IClientSession
     *
     * @author pangchong
     *
     */
    static class GameSession implements IClientSession {

        // session存储的数据
        /**由所在服务器生成，自增的（由于long足够大，哪怕每秒1000个连接，也可以连续坚持几亿年，所以就不考虑循环使用的问题了）*/
        private final long sessionId;
        private final ChannelHandlerContext ctx;
        private int targetGameServerId;
        private volatile long uuid;
        /** 建立连接时的时间*/
        private long connectedTime;

        public GameSession(ChannelHandlerContext ctx) {
            this.ctx = ctx;
            sessionId = IdGenerator.getNextId();
            connectedTime = TimeService.Inst.now();
        }

        @Override
        public void sendMessage(SCMessage msg) {
            if (log.isDebugEnabled()) {
                log.debug("[[[Send message]]]: session - {}, code - {}, type - {}", getSessionId(),
                        msg.messageType, MessageType.GCMessageType.valueOf(msg.messageType));
            }
            ctx.writeAndFlush(msg);
        }

        @Override
        public void sendMessage(GeneratedMessage msg) {
            SCMessage scmsg = new SCMessage(
                    msg.getDescriptorForType().getOptions().getExtension(MessageType.gcMessageType)
                            .getNumber(), msg.toByteArray());
            sendMessage(scmsg);
        }

        @Override
        public <T extends GeneratedMessage.Builder<T>> void sendMessage(GeneratedMessage.Builder<T> msg) {
            sendMessage((GeneratedMessage) msg.build());
        }

        @Override
        public void disconnect() {
            ctx.disconnect();
        }

        @Override
        public long getSessionId() {
            return sessionId;
        }

        @Override
        public int getTargetGameServerId() {
            return targetGameServerId;
        }

        @Override
        public void setTargetGameServerId(int gameServerId) {
            this.targetGameServerId = gameServerId;
        }

        @Override
        public long getUuid() {
            return this.uuid;
        }

        @Override
        public void setUuid(long uuid) {
            this.uuid = uuid;
        }

        @Override
        public long getConnectedTime() {
            return connectedTime;
        }

        @Override
        public boolean isInActive() {
            return !ctx.channel().isActive();
        }

        @Override
        public String getClientAddress() {
            return ctx.channel().localAddress().toString();
        }
    }
}
