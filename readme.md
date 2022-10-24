### Description
This project demonstrates how to use a topic with two partitions and still have a consistent way of routing events.

We do have 2 apps:

- order-app: exposes 2 REST endpoints, one to place a new order, and another to cancel it.
  When a place order is received, it saves it to a PostgreSQL database, sends a Kafka record to the "orders" topic
  (which has two partitions), and sends a response back to the UI.
- billing-app: consumes from Kafka topic "orders" and prints them to the console. If only one billing-app is
    started, it will consume from both partitions of the "orders" topic, and will write in the console log each record consumed,
    if 2 instances of billing-app are run, each instance will consume only from one of the partitions. 
    If one of the instances dies, the other will take over and continue listening from the partition that was assigned
    to the dead billing-app. If later on, a new instance of billing-app is started, partitions will be splitted amongst 
    the two billing-app instances.

### How to run it
Run the following command within the folder [PROJECT_ROOT_FOLDER]/docker:
`docker compose up -d`

It will start a PostgreSQL database docker container (`db`), a database admin docker container (`admin`)
and a few Kafka related docker containers:
- `schema-registry-1`: keeps track of the different versions of the messages sent to the "orders" topic 
  (in this project there's only one, but typically they evolve over time)
- `kafka-1` broker
- `zookeeper-1` 

Then start the OrderManagementApplication and the BillingApplication (you can start one or two instances of the billing-app)

The frontend UI can be started by changing to folder [PROJECT_ROOT_FOLDER]/ui and executing `npm run dev`, then open a 
web browser and navigate to `http://localhost:5173/`

Alternatively, execute the cucumber tests in file order-app/src/test/resources/bdd/order_management.feature


