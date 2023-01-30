package uz.cbssolutions.broker.error;

public class SubscriberNotFoundException extends Exception {

    public SubscriberNotFoundException(String topic) {
        super("Subscriber for topic '" + topic + "' not found");
    }
}
