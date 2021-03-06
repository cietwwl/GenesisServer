package com.genesis.robot.core;

import com.genesis.robot.core.config.RobotConfig;
import com.genesis.robot.core.msgfunc.GCMsgFunctionService;
import com.genesis.robot.core.msgfunc.GCMsgFunctionUtil;
import com.genesis.robot.core.msgfunc.IGCMsgFunc;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Parser;
import com.genesis.common.core.GlobalData;
import com.genesis.core.event.EventBus;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * 机器人需要的各种全局数据
 * @author Joey
 *
 */
public class Global {

    /**消息解析器*/
    private static GCMsgFunctionService gcMsgFuncService;
    /**事件管理器*/
    private static EventBus eventBus;


    public static void init(boolean haveUI) {
        GlobalData.init(RobotConfig.getBaseResourceDir(), RobotConfig.isXorLoad());

        if (haveUI) {
            Pair<Map<Integer, IGCMsgFunc<GeneratedMessage>>, Map<Integer, Parser<? extends GeneratedMessage>>>
                    pair = GCMsgFunctionUtil.buildMsgFunction("com.mokylin.bleach.robot.func.ui");
            gcMsgFuncService = new GCMsgFunctionService(pair.getLeft(), pair.getRight());
        } else {
            Pair<Map<Integer, IGCMsgFunc<GeneratedMessage>>, Map<Integer, Parser<? extends GeneratedMessage>>>
                    pair = GCMsgFunctionUtil.buildMsgFunction("com.mokylin.bleach.robot.func.noui");
            gcMsgFuncService = new GCMsgFunctionService(pair.getLeft(), pair.getRight());
        }


        //		{
        //			Map<Integer, SpellActionCalculaeTemplate> map = GlobalData.getTemplateService().getAll(SpellActionCalculaeTemplate.class);
        //			for (SpellActionCalculaeTemplate template : map.values()) {
        //				List<EffectBase> list = template.getAttributeList();
        //				for (EffectBase effectBase : list) {
        //					System.out.println(effectBase.getEffectName());
        //					int[] params = effectBase.getParams();
        //					if (params==null) {
        //						continue;
        //					}
        //					for (int i = 0; i < params.length; i++) {
        //						System.out.print(params[i]);
        //						System.out.print("	");
        //					}
        //					System.out.println();
        //				}
        //			}
        //
        //			System.out.println("==========================================================");
        //		}
        //
        //
        //		//Buff
        //		{
        //			Map<Integer, BuffTemplate> map = GlobalData.getTemplateService().getAll(BuffTemplate.class);
        //			for (BuffTemplate template : map.values()) {
        //				EffectBase effectBase = template.getEffectMain();
        //				System.out.println(effectBase.getEffectName());
        //				int[] params = effectBase.getParams();
        //				if (params==null) {
        //					continue;
        //				}
        //				for (int i = 0; i < params.length; i++) {
        //					System.out.print(params[i]);
        //					System.out.print("	");
        //				}
        //				System.out.println();
        //			}
        //			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        //		}
        //
        //		{
        //			Map<Integer, BuffTemplate> map = GlobalData.getTemplateService().getAll(BuffTemplate.class);
        //			for (BuffTemplate template : map.values()) {
        //				List<TempAttrNode3Col> list = template.getAttributeList();
        //				for (TempAttrNode3Col tempAttrNode3Col : list) {
        //					System.out.print(tempAttrNode3Col.getAttributeIndex());
        //					System.out.print("	");
        //					System.out.print(tempAttrNode3Col.getAbsValue());
        //					System.out.print("	");
        //					System.out.println(tempAttrNode3Col.getPerValue());
        //				}
        //			}
        //			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        //		}
        //
        //		{
        //			Map<Integer, ItemTemplate> map = GlobalData.getTemplateService().getAll(ItemTemplate.class);
        //			for (ItemTemplate itemTemplate : map.values()) {
        //				int[] arr = itemTemplate.getFromMissionTemplateIds();
        //				System.out.print(itemTemplate.getName());
        //				System.out.print("	");
        //				for (int i : arr) {
        //					System.out.print(i);
        //					System.out.print("	");
        //				}
        //				System.out.println();
        //			}
        //			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        //		}
        //
        //		{
        //			Map<Integer, CostByCountsTemplate> map = GlobalData.getTemplateService().getAll(CostByCountsTemplate.class);
        //			for (CostByCountsTemplate template : map.values()) {
        //				long[] arr = template.getCostByCountsArray();
        //				for (long i : arr) {
        //					System.out.print(i);
        //					System.out.print("	");
        //				}
        //				System.out.println();
        //			}
        //		}
    }

    public static GCMsgFunctionService getGCMsgFuncService() {
        return gcMsgFuncService;
    }

    public static EventBus getEventBus() {
        return eventBus;
    }
}
