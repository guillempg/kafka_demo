package com.example.order.bdd.testcontainers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;


public class PostgresContainer extends GenericContainer<PostgresContainer> { //PostgreSQLContainer<PostgresContainer> {

    private final String networkAlias = "postgres";
    public static final Logger POSTGRES_LOGGER = LoggerFactory.getLogger("POSTGRES-LOGGER");
    public static final String DB_USER = "user";
    public static final String DB_PASSWORD = "secret";
    public static final String DB_DATABASE = "db";

    private static final int POSTGRESQL_PORT = 5432;

    public PostgresContainer() {
        super(DockerImageName.parse("postgres:9.6.12"));
    }

    @Override
    protected void configure() {
        addEnv("POSTGRES_DB", DB_DATABASE);
        addEnv("POSTGRES_USER", DB_USER);
        addEnv("POSTGRES_PASSWORD", DB_PASSWORD);
        addFixedExposedPort(POSTGRESQL_PORT, POSTGRESQL_PORT);
    }

    @Override
    public void start() {
        super.start();
        this.followOutput(new Slf4jLogConsumer(POSTGRES_LOGGER));
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://" +
                getHost() +
                ":" +
                getMappedPort(POSTGRESQL_PORT) +
                "/" +
                DB_DATABASE;

    }
}

