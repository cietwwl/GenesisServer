package com.genesis.test.core.serviceinit.servicefilter;

import com.genesis.core.serviceinit.ServiceInitializeRequired;

public class Service14 implements ServiceInitializeRequired {

    private static Service14 Instance3 = new Service14();

    private Service14() {
    }

    public static Service14 service() {
        return Instance3;
    }

    @Override
    public void init() {
        StatisticsFilter.service14Inited = true;
    }

}
