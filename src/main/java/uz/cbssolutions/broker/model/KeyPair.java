package uz.cbssolutions.broker.model;

public record KeyPair<TKey, TValue>(TKey key, TValue value) {
}
