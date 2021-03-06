package com.genesis.test.core.serviceinit.servicefilter;

import com.genesis.core.serviceinit.ServiceInitializeRequired;

public class Service15 implements ServiceInitializeRequired {

    private static Service15 Instance = new Service15();

    private Service15() {
    }

    public static Service15 service() {
        return Instance;
    }

    @Override
    public void init() {
        StatisticsFilter.service15Inited = true;
    }

}
