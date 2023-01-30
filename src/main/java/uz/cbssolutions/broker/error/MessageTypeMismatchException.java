package uz.cbssolutions.broker.error;

public class MessageTypeMismatchException extends Exception {
    public MessageTypeMismatchException(Class<?> msgClass) {
        super("Message must be of type '" + msgClass.getTypeName() + "'");
    }
}
