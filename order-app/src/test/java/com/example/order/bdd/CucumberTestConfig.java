package com.example.order.bdd;

import com.example.order.OrderManagementApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
//@EmbeddedKafka(partitions = 2)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
        classes = {OrderManagementApplication.class},
        initializers = {TestContainersInitializer.class}
)
public class CucumberTestConfig {

}
