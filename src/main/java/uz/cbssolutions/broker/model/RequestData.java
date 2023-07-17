package uz.cbssolutions.broker.model;

import java.util.Map;

public record RequestData(String topic, String jsonBody, Map<String, Object> headers) {
}
