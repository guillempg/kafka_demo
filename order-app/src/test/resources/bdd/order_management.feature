Feature: Order management

  Background:
    Given components "DATABASE,KAFKA" are running

  Scenario: Order management application receives orders and publishes order_placed event
    Given kafka topic 'TopicA' with 1 partition exists
    When the following order is placed through REST with tracking id "trackingId-1":
      | orderType | owner          | symbol | quantity | side |
      | market    | Wile E. Coyote | ACME   | 100      | BUY  |
    Then the response of request with tracking id "trackingId-1" is:
      | orderType | owner          | symbol | quantity | side |
      | market    | Wile E. Coyote | ACME   | 100      | BUY  |
    And the order corresponding to tracking id "trackingId-1" is in the database:
      | orderId         | owner          | orderType | symbol | quantity | side |
      | #ID_PLACEHOLDER | Wile E. Coyote | market    | ACME   | 100      | BUY  |
    And the following event corresponding to tracking id "trackingId-1" is published in kafka topic 'OrderPlaced':
      | orderType | orderId         | symbol | quantity | side |
      | market    | #ID_PLACEHOLDER | ACME   | 100      | BUY  |

  Scenario: Order management application receives several orders and publishes their order_placed event
    When the following orders are successfully placed through REST:
      | orderType | owner                  | symbol | quantity | side |
      | market    | Wile E. Coyote         | ACME   | 100      | BUY  |
      | market    | Little Red Riding Hood | JELLY  | 101      | BUY  |
      | market    | Bugs Bunny             | CARROT | 102      | BUY  |
      | market    | Daffy Duck             | LCK    | 103      | BUY  |
      | market    | Marvin                 | MRS    | 104      | BUY  |
    And all previous orders are in the database
    Then the following order placed events are published in kafka topic 'orders':
      | orderType | orderId         | symbol | quantity | side |
      | market    | #ID_PLACEHOLDER | ACME   | 100      | BUY  |
      | market    | #ID_PLACEHOLDER | JELLY  | 101      | BUY  |
      | market    | #ID_PLACEHOLDER | CARROT | 102      | BUY  |
      | market    | #ID_PLACEHOLDER | LCK    | 103      | BUY  |
      | market    | #ID_PLACEHOLDER | MRS    | 104      | BUY  |

  Scenario: A given consumer of a consumer group only reads from one partition of the topic
    Given kafka topic 'TopicB' with 2 partition exists
    And the following consumers from the same consumer group subscribe to topic 'TopicB':
      | name      | partitionId |
      | consumerA | 0           |
      | consumerB | 1           |
    When the following order placed events are published in kafka topic 'TopicB':
      | orderType | orderId | symbol | quantity | side |
      | market    | 1       | ACME   | 100      | BUY  |
      | market    | 2       | JELLY  | 101      | BUY  |
      | market    | 3       | CARROT | 102      | BUY  |
      | market    | 4       | LCK    | 103      | BUY  |
      | market    | 5       | MRS    | 104      | BUY  |
    Then consumer 'consumerA' of kafka topic 'TopicB' consumes OrderPlaced messages:
      | orderId | symbol | quantity | side | orderType |
      | 2       | JELLY  | 101      | BUY  | market    |
      | 4       | LCK    | 103      | BUY  | market    |
    And consumer 'consumerB' of kafka topic 'TopicB' consumes OrderPlaced messages:
      | orderId | symbol | quantity | side | orderType |
      | 1       | ACME   | 100      | BUY  | market    |
      | 3       | CARROT | 102      | BUY  | market    |
      | 5       | MRS    | 104      | BUY  | market    |

  Scenario: Order events for a given order are consistently sent to the same partition
    Given kafka topic 'TopicC' with 2 partition exists
    And the following consumers from the same consumer group subscribe to topic 'TopicC':
      | name      | partitionId |
      | consumerA | 0           |
      | consumerB | 1           |
    When the following order placed events are published in kafka topic 'TopicC':
      | orderType | orderId | symbol | quantity | side |
      | market    | 1       | ACME   | 100      | BUY  |
      | market    | 2       | JELLY  | 101      | BUY  |
      | market    | 3       | CARROT | 102      | BUY  |
      | market    | 4       | LCK    | 103      | BUY  |
    And the following order cancelled events are published in kafka topic 'TopicC':
      | orderId | user     |
      | 1       | whatever |
      | 2       | whatever |
    And the following order filled events are published in kafka topic 'TopicC':
      | orderId | counterparty | quantity |
      | 3       | whatever     | 100      |
      | 4       | whatever     | 101      |
    Then consumer 'consumerA' of kafka topic 'TopicC' consumes messages:
      | msgType  | orderId |
      | PLACED   | 2       |
      | PLACED   | 4       |
      | CANCELED | 2       |
      | FILLED   | 4       |
    And consumer 'consumerB' of kafka topic 'TopicC' consumes messages:
      | msgType  | orderId |
      | PLACED   | 1       |
      | PLACED   | 3       |
      | CANCELED | 1       |
      | FILLED   | 3       |

  Scenario: Order management application consumes order_placed events
    When the following event is published in kafka topic 'orders':
      | orderType | orderId | symbol | quantity | side |
      | market    | 2       | ACME   | 100      | SELL |
    Then the following event is consumed from kafka topic 'orders':
    """
    {"orderId": "2", "orderType": "MARKET", "symbol": "ACME", "quantity": 100, "side": "SELL"}
    """