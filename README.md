# Broker plugin

These plugins have been created to simplify subscribe/publish pattern of JMS in reactive manner.

## Plugin configuration

To configure plugin do the steps described below:

- Include the ***broker*** dependency into your ***pom.xml***.

```xml

<dependency>
    <groupId>uz.cbssolutions</groupId>
    <artifactId>broker</artifactId>
    <version>VERSION</version>
</dependency>
```

> Where ***VERSION*** is the latest version of ***broker*** plugin. You can check it out in ***The Package Registry***.

- Configure connection parameters in your ***application.yml***.

```yml
cbs-broker:
  url: tcp://localhost:61616/
  user: admin
  password: admin
```

> - ***url*** - the broker's actual address.
> - ***username*** - username registered in the broker's system.
> - ***password*** - password of the user discribed above.

## Publish

Message publishing in reactive manner via ***this*** plugin is pretty easy.

First of all, you must inject instance of ***Publisher*** interface. Then somewhere of your reactive downstream call the
***publish(..)*** method. Pass the ***topic name***, ***body*** and ***headers*** into it. That's it!

```java
/**
 * Dummy class.
 */
@RequiredArgsConstructor
class Dummy {

    /**
     * Instance injection of {@code Publisher}.
     */
    private final Publisher publisher;

    /**
     * Main action method.
     *
     * @param data Some dummy data.
     * @return Operation end signal.
     */
    public Mono<Void> dummy(String data) {
        return echo(data)
                .flatMap(result -> publisher.publish("dummy-topic", result, this.getHeaders()));
    }

    /**
     * Gets predefined headers.
     *
     * @return Headers.
     */
    public Map<String, Object> getHeaders() {
        var map = new HashMap<String, Object>();
        map.put("dummy-header", "dummy-value");
        return map;
    }

    /**
     * Returns what receives.
     *
     * @param str Some text data.
     * @return Received data.
     */
    public Mono<String> echo(String str) {
        return Mono.just(str);
    }
}
```

## Subscribe

Topic subscription is a bit complexer than [message publishing](#Publish).
To subscribe to some topic you must implement the ***Subscriber*** interface. You will see that the ***Subscriber***
interface has three members. The ***getTopic()*** method returns the name of the topic to which you want to subscribe.
***getMsgClass()*** method returns the class of the object that you expect in the message's body. And ***handle(..)***
method handles an incoming message.

> Note that expected message (i.e. ***TRequestModel***) of the ***Subscriber*** interface must implement the
***Serializable*** interface.

```java
/**
 * Dummy subscriber implementation.
 */
public class DummySubscriber implements Subscriber<DummyModel> {

    /**
     * Gets message body's class.
     *
     * @return Class of message's body.
     */
    @Override
    public Class<DummyModel> getMsgClass() {
        return DummyModel.class;
    }

    /**
     * Gets the topic name.
     *
     * @return Topic name.
     */
    @Override
    public String getTopic() {
        return "dummy-topic";
    }

    /**
     * Incoming message handler.
     *
     * @param message Incoming message.
     * @return End operation signal.
     */
    @Override
    public Mono<Void> handle(Message<DummyModel> message) {
        System.out.println(message.model());
        return Mono.empty();
    }
}

/**
 * Expected Message's body.
 *
 * @param str Dummy text data.
 */
public record DummyModel(String str) implements Serializable {
}
```