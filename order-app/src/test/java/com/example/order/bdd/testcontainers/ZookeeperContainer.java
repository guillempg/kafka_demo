package com.example.order.bdd.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.HashMap;

import static com.example.order.bdd.TestContainersInitializer.CONFLUENT_PLATFORM_VERSION;
import static java.lang.String.format;

public class ZookeeperContainer extends GenericContainer<ZookeeperContainer> {

    private static final int ZOOKEEPER_INTERNAL_PORT = 2181;
    private static final int ZOOKEEPER_TICK_TIME = 2000;

    private final String networkAlias = "zookeeper";

    public ZookeeperContainer() {
        this(CONFLUENT_PLATFORM_VERSION);
    }

    public ZookeeperContainer(String confluentPlatformVersion) {
        super(getZookeeperContainerImage(confluentPlatformVersion));

        var env = new HashMap<String, String>();
        env.put("ZOOKEEPER_CLIENT_PORT", Integer.toString(ZOOKEEPER_INTERNAL_PORT));
        env.put("ZOOKEEPER_TICK_TIME", Integer.toString(ZOOKEEPER_TICK_TIME));
        withEnv(env);

        addExposedPort(ZOOKEEPER_INTERNAL_PORT);
        withNetworkAliases(networkAlias);
    }

    public String getInternalUrl() {
        return format("%s:%d", networkAlias, ZOOKEEPER_INTERNAL_PORT);
    }

    private static String getZookeeperContainerImage(String confluentPlatformVersion) {
        return (String) TestcontainersConfiguration
                .getInstance().getProperties().getOrDefault(
                        "zookeeper.container.image",
                        "confluentinc/cp-zookeeper:" + confluentPlatformVersion
                );
    }
}
