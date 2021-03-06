package com.genesis.loginserver.handlers;

import com.genesis.network2client.auth.AuthUtil;
import com.genesis.network2client.process.IClientMsgHandler;
import com.genesis.loginserver.core.version.Version;
import com.genesis.redis.center.data.GateInfo;
import com.genesis.redis.center.data.LoginClientInfo;
import com.genesis.redis.center.RedisLoginKey;
import com.genesis.protobuf.LoginMessage;
import com.genesis.servermsg.core.isc.ServerIdDef;
import com.genesis.servermsg.core.isc.ServerType;
import com.genesis.core.util.RandomUtil;
import com.genesis.loginserver.globals.Globals;
import com.genesis.network2client.session.IClientSession;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.Map;

/**
 * 客户端登陆
 * <p>LoginServer的负载均衡策略：
 * <p>由客户端在配置中的几个LoginServer间随机选择（客户端每次启动后用不同的随机数种子）；
 * <p>一旦某个LoginServer人数过多，而其他LoginServer又有余量，则会指派客户端登陆有余量的LoginServer(不确定这样做是否有意义)
 * <p>2018-02-08 18:57
 * @author Joey
 **/
public class CSLoginHandler implements IClientMsgHandler<LoginMessage.CSLogin> {


    @Override
    public void handle(IClientSession session, LoginMessage.CSLogin csLogin) {
        final String accountId = csLogin.getAccountId();
        final String channel = csLogin.getChannel();
        final String key = csLogin.getKey();
        final String macAddress = csLogin.getMacAddress();
        final String version = csLogin.getVersion();

        //0.0 检验版本
        if (!Version.Inst.isAllowed(version)) {
            // 无论是谁，版本不一致，都不允许登陆
            notifyFailAndDisconnect(session, LoginMessage.LoginFailReason.VERSION_NOT_ALLOW);
            return;
        }

        //0.1 检测该账号是否是GM账号
        if (isGM(channel, accountId)) {
            // GM有特权，可以直接到认证这一步
            auth(session, channel, accountId, key);
            return;
        }

        //1.0 开始验证
        //1.1 检查服务器是否开放
        if (!Globals.isIsServerOpen()) {
            session.sendMessage(LoginMessage.SCLoginServerNotOpen.getDefaultInstance());
            session.disconnect();
            return;
        }
        //1.2 检查整个大区是否人满 TODO

        //1.3 检查本服务器是否人满（如果满了而其他LoginServer有余量，则给玩家指定另一个LoginServer） TODO

        //1.4 如果是封测，检验账号是否激活 TODO

        //2.0 认证
        auth(session, channel, accountId, key);

        //需要确认是否可以直接断开连接（怕玩家收不到刚发的消息），如果不行就要注册Timer
    }

    /**
     * 登陆认证及后续逻辑
     * @param channel
     * @param accountId
     * @param key
     */
    private void auth(IClientSession session, String channel, String accountId, String key) {
        // 1.认证
        if (Globals.getLoginConfig().isLocalAuth()) {
            // 本地认证
            if (!authLocal(session, channel, accountId, key))
                return;
        } else {
            // 平台认证
            if (!authPlatform(session, channel, accountId, key))
                return;
        }

        // 2.0 处理全球服登陆事宜（以下流程，当准备部署第二个地理区域时，再做） TODO
        // 2.1 总登陆中心加锁（如果失败，则踢下线）
        // 2.2 查询上次登陆，是否在本数据中心
        // 2.2.1 是：跳到流程2.3
        // 2.2.2 否：开始走跨区域加载数据流程(此流程走完之后，进入流程2.3)
        // 2.3 置为在线状态，然后总登陆中心解锁（如果失败，则踢下线）（锁存在超时时间，暂定1分钟）

        // 4.0 分配网关
        // 加锁
        if (!AuthUtil.lock(channel, accountId, Globals.getRedisson())) {
            notifyFailAndDisconnect(session, LoginMessage.LoginFailReason.YOUR_ACCOUNT_LOGIN_ON_OTHER_SERVER);
            return;
        }

        // 4.1 查询是否已经在某个Gate上了
        final int gateID = findGate(channel, accountId);
        if (gateID > ServerIdDef.InvalidServerID) {
            // 将此Client定向到这台Gate上，取代旧连接
            if (!selectGateById(session, gateID, channel, accountId)) {
                notifyFailAndDisconnect(session, LoginMessage.LoginFailReason.YOUR_ACCOUNT_LOGIN_ON_OTHER_SERVER);
            }
            return;
        }

        // 4.2 分配一个Gate给此玩家
        allotGate(session, channel, accountId);

        // 解锁
        if (!AuthUtil.unlock(channel, accountId, Globals.getRedisson())) {
            notifyFailAndDisconnect(session, LoginMessage.LoginFailReason.YOUR_ACCOUNT_LOGIN_ON_OTHER_SERVER);
            return;
        }
    }

    /**
     * 查询该账号是否已经在某Gate上
     * @param channel
     * @param accountId
     * @return  找到的GateID
     */
    private int findGate(String channel, String accountId) {
        final String key = RedisLoginKey.InGate.builderKey(channel, accountId);
        final RedissonClient redisson = Globals.getRedisson();
        final RBucket<Integer> bucket = redisson.getBucket(key);
        if (bucket.isExists()) {
            return bucket.get();
        }
        return ServerIdDef.InvalidServerID;
    }

    /**
     * 指定某Gate，供该玩家登陆
     * @param session
     * @param gateID
     * @param channel
     * @param accountId
     * @return 是否成功
     */
    private boolean selectGateById(IClientSession session, int gateID, String channel, String accountId) {
        final String gateKey = ServerType.GATE.getKey();
        final RedissonClient redisson = Globals.getRedisson();
        RMap<Integer, GateInfo> map = redisson.getMap(gateKey);

        GateInfo gateInfo = map.get(gateID);
        if (gateInfo==null)
            return false;

        selectGate(session, channel, accountId, gateInfo);
        return true;
    }

    /**
     * 分配Gate
     * @param session
     * @param channel
     * @param accountId
     */
    private void allotGate(IClientSession session, String channel, String accountId) {
        final String gateKey = ServerType.GATE.getKey();
        final RedissonClient redisson = Globals.getRedisson();
        RMap<Integer, GateInfo> map = redisson.getMap(gateKey);
        double persent = 1.0;   // 1.0即100%，表示满载
        long gateID = 0;        // 0为无效ID，有效ID是正整数
        GateInfo gateInfo = null;
        for (Map.Entry<Integer, GateInfo> entry : map.entrySet()) {
            final GateInfo value = entry.getValue();
            double tempPer = value.currClientCount * 1.0 / value.maxClientCount;
            if (tempPer < persent) {
                persent = tempPer;
                gateID = entry.getKey();
                gateInfo = value;
            }
        }
        if (gateID > 0) {
            selectGate(session, channel, accountId, gateInfo);

        } else {
            // Gate全部人满
            this.notifyFailAndDisconnect(session, LoginMessage.LoginFailReason.PLAYER_IS_FULL);
        }
    }

    /**
     * 选择指定的Gate
     * @param session
     * @param channel
     * @param accountId
     * @param gateInfo
     */
    private void selectGate(IClientSession session, String channel, String accountId, GateInfo gateInfo) {
        // 生成验证码
        final int length = 16;
        ArrayList<Integer> vCode = new ArrayList<>(length);   // 验证码
        for (int i = 0; i < length; i++) {
            final int nextInt = RandomUtil.nextInt(Integer.MAX_VALUE);
            vCode.add(nextInt);
        }

        // 先写入LoginRedis，之后由Gate读取
        LoginClientInfo loginClientInfo = new LoginClientInfo(channel, accountId, vCode);
        final RedissonClient redisson = Globals.getRedisson();
        // 组装待登陆的玩家key
        final String keyToGate = RedisLoginKey.ToGate.builderKey(channel, accountId);
        final RBucket<LoginClientInfo> bucket = redisson.getBucket(keyToGate);
        bucket.set(loginClientInfo);

        // 再通知Client
        final LoginMessage.SCLoginSuccess.Builder builder = LoginMessage.SCLoginSuccess.newBuilder();
        builder.setGateIP(gateInfo.netInfoToClient.getHost())
                .setGatePort(gateInfo.netInfoToClient.getPort())
                .addAllVerificationCode(vCode);
        session.sendMessage(builder);
    }

    /**
     *
     * @return true：进入排队或队列满断开连接； <p>false：无需排队。
     */
    private boolean enqueue() {
        return false;
    }

    /**
     * 是否为GM账号（此处只查询账号属性，不做账号认证）
     * @param channel
     * @param accountId
     * @return
     */
    private boolean isGM(String channel, String accountId) {
        // 去LoginRedis中查询，该账号是否为GM账号 TODO
        return false;
    }

    /**
     * 通知客户端，登陆失败。并断开连接
     * @param session   客户端连接
     * @param reason    失败原因
     */
    private void notifyFailAndDisconnect(IClientSession session, LoginMessage.LoginFailReason reason) {
        final LoginMessage.SCLoginFail.Builder builder = LoginMessage.SCLoginFail.newBuilder();
        builder.setFailReason(reason);
        session.sendMessage(builder);

        // 断开连接
        session.disconnect();
    }

    /**
     * 本地认证
     * @param session   客户端连接
     * @param channel   渠道
     * @param account   账号ID
     * @param key       验证字串(本地认证模式下，必须为"",才允许登陆)
     * @return  是否认证通过
     */
    private boolean authLocal(IClientSession session, String channel, String account, String key) {
        // 这是为了在外网登录墙开启的情况下，强行登录GM账号进入游戏而做的
        if (key != null) {
            // 通知客户端，服务器尚未开放 TODO
            session.disconnect();
            return false;
        }

        // 1.0 在缓存中查询，此账号是否存在
        //		final IRedis iRedis = Globals.getiRedis();
        //		//<服，渠道+账号，Account>
        //		iRedis.getHashOp().hsetnx(key, field, value)

        return true;
    }

    /**
     * 平台认证
     * @param session   客户端连接
     * @param channel   渠道
     * @param account   账号ID
     * @param key       验证字串
     * @return  是否认证通过
     */
    private boolean authPlatform(IClientSession session, String channel, String account, String key) {
        // TODO Auto-generated method stub
        return false;
    }
}
