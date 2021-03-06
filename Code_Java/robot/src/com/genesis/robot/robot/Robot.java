package com.genesis.robot.robot;

import com.genesis.protobuf.HumanMessage.GCHumanDetailInfo;
import com.genesis.robot.human.Human;

import io.netty.channel.ChannelHandlerContext;

public class Robot extends RobotSession {

    private Human human = null;

    public Robot(ChannelHandlerContext ctx) {
        super(ctx);
    }

    public void initHuman(GCHumanDetailInfo msg) {
        human = new Human(this);
        human.init(msg);
    }

    public Human getHuman() {
        return this.human;
    }

}
