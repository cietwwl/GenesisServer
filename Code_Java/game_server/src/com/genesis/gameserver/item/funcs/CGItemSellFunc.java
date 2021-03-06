package com.genesis.gameserver.item.funcs;

import com.genesis.common.core.GlobalData;
import com.genesis.common.item.template.ItemTemplate;
import com.genesis.core.util.MathUtils;
import com.genesis.gameserver.core.global.ServerGlobals;
import com.genesis.gameserver.core.msgfunc.AbstractClientMsgFunc;
import com.genesis.gameserver.human.Human;
import com.genesis.gameserver.player.Player;
import com.genesis.protobuf.ItemMessage.CGItemSell;

/**
 * 处理客户端使用物品消息的函数对象。
 *
 * 该函数对象在PlayerActor中执行。
 *
 * @author pangchong
 *
 */
public class CGItemSellFunc extends AbstractClientMsgFunc<CGItemSell, Human, ServerGlobals> {

    @Override
    public void handle(Player player, CGItemSell msg, Human human, ServerGlobals sGlobals) {

        ItemTemplate itemTemplate =
                GlobalData.getTemplateService().get(msg.getTemplateId(), ItemTemplate.class);
        if (itemTemplate == null) {
            human.notifyDataErrorAndDisconnect();
            return;
        }

        if (human.getInventory().deleteItem(msg.getTemplateId(), msg.getAmount())) {
            long total = msg.getAmount() * itemTemplate.getSellPrice();
            if (MathUtils.longMultiplyOverflow(msg.getAmount(), itemTemplate.getSellPrice())) {
                total = Long.MAX_VALUE;
            }
            human.giveMoney(itemTemplate.getCurrencyPropId(), total);
        } else {
            human.notifyDataErrorAndDisconnect();
        }
    }

}
