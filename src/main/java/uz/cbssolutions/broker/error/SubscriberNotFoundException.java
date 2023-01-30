package uz.cbssolutions.broker.error;

public class SubscriberNotFoundException extends Exception {
    public SubscriberNotFoundException() {
        super("Subscriptions are missing (or not implemented)");
    }
}
