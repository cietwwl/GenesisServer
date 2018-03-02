package com.mokylin.bleach.gameserver.shop.timeevent;

import com.mokylin.bleach.core.timeaxis.ITimeEventType;
import com.mokylin.bleach.core.timeaxis.TimeAxis;
import com.mokylin.bleach.gameserver.core.autoexecutetask.AbstractAutoExecuteTaskWithHandleObjectInSql;
import com.mokylin.bleach.gameserver.core.global.Globals;
import com.mokylin.bleach.gameserver.human.Human;
import com.mokylin.bleach.gameserver.shop.shop.Shop;
import com.genesis.protobuf.ShopMessage.GCShopManuallyRefreshCountReset;

import org.joda.time.LocalTime;

import java.sql.Timestamp;

/**
 * 商店的手动刷新
 * @author yaguang.xiao
 *
 */
public class ShopManuallyRefresh extends AbstractAutoExecuteTaskWithHandleObjectInSql<Human> {

    private Shop shop;
    /** 手动刷新次数(存库) */
    private int manuallyRefreshCount;

    public ShopManuallyRefresh(LocalTime autoExecuteTime, TimeAxis<Human> timeAxis, Shop shop,
            int manuallyRefreshCount, Timestamp lastResetManuallyRefreshCountTime) {
        super(autoExecuteTime, timeAxis, lastResetManuallyRefreshCountTime.getTime(), shop);
        this.shop = shop;
        this.manuallyRefreshCount = manuallyRefreshCount;
    }

    @Override
    public ITimeEventType getEventType() {
        return ShopTimeEventType.RESET_SHOP_MANUALLY_REFRESH_COUNT;
    }

    @Override
    public long getEventId() {
        return this.shop.getShopType().getIndex();
    }

    public int getManuallyRefreshCount() {
        return manuallyRefreshCount;
    }

    /**
     * 手动刷新
     * @param humanLevel
     */
    public void mauallyRefresh(int humanLevel) {
        long now = Globals.getTimeService().now();
        this.shop.getShopRefresh().refresh(humanLevel, now);
        this.manuallyRefreshCount++;
        this.shop.setModified();
    }

    @Override
    protected boolean isCanAutoExecute(Human human) {
        return human.getWindowManager().isOpen(shop.getShopType().getWindow());
    }

    @Override
    public void simplyExecute(long executeTime, Human host) {
        this.manuallyRefreshCount = 0;
    }

    @Override
    protected void sendMessage(Human human) {
        GCShopManuallyRefreshCountReset.Builder msg = GCShopManuallyRefreshCountReset.newBuilder();
        msg.setShopTypeId(shop.getShopType().getIndex());
        human.sendMessage(msg);
    }

}
