package com.genesis.test.core.serviceinit.servicecirculardep;

import com.genesis.core.serviceinit.Depend;
import com.genesis.core.serviceinit.ServiceInitializeRequired;

@Depend({Service8.class})
public class Service7 implements ServiceInitializeRequired {

    private static Service7 Instance = new Service7();

    private Service7() {
    }

    public static Service7 service() {
        return Instance;
    }

    @Override
    public void init() {

    }

}
