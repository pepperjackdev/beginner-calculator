package com.calculator.calculator;

import java.util.function.BinaryOperator;

public class Operation<T, K extends BinaryOperator<T>> {

    private final T value;
    private K operator;

    public Operation(T value, K operator) {
        this.value = value;
        this.operator = operator;
    }

    public T getValue() {
        return this.value;
    }

    public K getOperator() {
        return this.operator;
    }

    public void setOperator(K operator) {
        this.operator = operator;
    }

    public T getOperatedValue(T value) {
        return operator.apply(this.value, value);
    }

}
