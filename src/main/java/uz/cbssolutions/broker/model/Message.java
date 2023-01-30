package uz.cbssolutions.broker.model;

import java.util.Map;

public record Message<TModel>(TModel model, Map<String, String> headers) {
}
