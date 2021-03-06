package com.genesis.test.dataserver;

import com.genesis.core.orm.hibernate.HibernateDBService;
import com.genesis.gamedb.orm.entity.HumanEntity;
import com.genesis.test.dataserver.gamedb.HumanTable;

import org.junit.Test;

import java.net.URL;
import java.util.Properties;

public class MockDBTest {

    @Test
    public void test() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("h2_hibernate_test.cfg.xml");

        String[] dbResources = new String[]{"hibernate_query.xml"};
        HibernateDBService dbService =
                new HibernateDBService("com.mokylin.bleach.gamedb.orm.entity", new Properties(),
                        url, dbResources);

        new HumanTable().insertToDB();

        HumanEntity he = dbService.getById(HumanEntity.class, 1L);
        System.out.println(he.getName());
    }


}
