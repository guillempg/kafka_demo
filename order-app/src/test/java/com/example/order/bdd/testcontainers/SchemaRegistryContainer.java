package com.example.order.bdd.testcontainers;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.TestcontainersConfiguration;

import static com.example.order.bdd.TestContainersInitializer.CONFLUENT_PLATFORM_VERSION;


public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {
    private static final int SCHEMA_REGISTRY_INTERNAL_PORT = 8081;

    private final String networkAlias = "schema-registry";

    public SchemaRegistryContainer(String zookeeperConnect) {
        this(CONFLUENT_PLATFORM_VERSION, zookeeperConnect);
    }

    public SchemaRegistryContainer(String confluentPlatformVersion, String zookeeperConnect) {
        super(getSchemaRegistryContainerImage(confluentPlatformVersion));

        addEnv("SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL", zookeeperConnect);
        addEnv("SCHEMA_REGISTRY_HOST_NAME", "localhost");

        withExposedPorts(SCHEMA_REGISTRY_INTERNAL_PORT);
        withNetworkAliases(networkAlias);
        waitingFor(Wait.forHttp("/subjects"));
    }

    public String getUrl() {
        return String.format("http://%s:%d", this.getContainerIpAddress(), this.getMappedPort(SCHEMA_REGISTRY_INTERNAL_PORT));
    }


    private static String getSchemaRegistryContainerImage(String confluentPlatformVersion) {
        return (String) TestcontainersConfiguration
                .getInstance().getProperties().getOrDefault(
                        "schemaregistry.container.image",
                        "confluentinc/cp-schema-registry:" + confluentPlatformVersion
                );
    }
}

