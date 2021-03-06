package com.genesis.robot.item;

import com.genesis.common.core.GlobalData;
import com.genesis.common.item.IItem;
import com.genesis.common.item.template.ItemTemplate;
import com.genesis.protobuf.db.DBInventoryBlob.ItemData;
import com.genesis.robot.human.Human;

public class Item implements IItem {

    private Human human;
    /**道具模板 */
    private ItemTemplate template;
    /**叠加数量*/
    private int overlap;

    private Item() {
    }

    public static Item buildFromItemEntity(ItemData itemData, Human human) {
        final int templateId = itemData.getTemplateId();
        ItemTemplate template = GlobalData.getTemplateService().get(templateId, ItemTemplate.class);
        Item item = new Item();
        item.human = human;
        item.template = template;
        item.fromItemData(itemData);
        return item;
    }

    public static Item buildNewItem(int templateId, int amount, Human human) {
        ItemTemplate template = GlobalData.getTemplateService().get(templateId, ItemTemplate.class);
        Item item = new Item();
        item.human = human;
        item.template = template;
        item.setOverlap(amount);
        return item;
    }

    public int getOverlap() {
        return this.overlap;
    }

    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }

    public Integer getTemplateId() {
        return template != null ? template.getId() : -1;
    }

    public ItemTemplate getTemplate() {
        return this.template;
    }

    public Human getOwner() {
        return human;
    }

    private void fromItemData(ItemData itemData) {
        this.overlap = itemData.getOverlap();
    }

    /**
     * @return 是否可以合成（当本道具是碎片时，才有可能返回true）
     */
    public boolean isCanCompound() {
        // TODO Auto-generated method stub
        return false;
    }

}
