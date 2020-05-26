[![codecov](https://codecov.io/gh/dilaverdemirel/spring-cloud-stream-outbox-extension/branch/master/graph/badge.svg?token=107NB0GV86)](https://codecov.io/gh/dilaverdemirel/spring-cloud-stream-outbox-extension)
![](https://github.com/dilaverdemirel/spring-cloud-stream-outbox-extension/workflows/Java%20CI/badge.svg)

# Spring Cloud Stream Outbox Extension

This library provides an extension if you already use spring cloud stream with the transactional database for application messaging.

There are three steps;

* Send a message with the message wrapper object(OutboxMessageEvent) over spring **ApplicationEventPublisher**
* **OutboxMessageHandler** catches the message to save to DB in same transaction
* **OutboxMessagePublisher** catches and send the message to binder(RabbitMQ or Kafka) after the transaction complete

You can see sequence diagram below.

![Sequence diagram](docs/resources/outbox-extension-diagram.png)