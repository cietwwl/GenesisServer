package com.genesis.gameserver.server.init;

import com.genesis.gamedb.orm.entity.ServerStatusEntity;
import com.genesis.gameserver.core.humaninfocache.HumanInfoCache;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServerActorInitResult {

    public final HumanInfoCache humanInfoCache;

    public final HashMap<Long, Integer> human2originalServerId;

    public final ServerStatusEntity serverStatusEntity;

    public ServerActorInitResult(HumanInfoCache humanInfoCache,
            HashMap<Long, Integer> human2originalServerId, ServerStatusEntity serverStatusEntity) {
        this.humanInfoCache = checkNotNull(humanInfoCache);
        this.human2originalServerId = checkNotNull(human2originalServerId);
        this.serverStatusEntity = serverStatusEntity;
    }

}
