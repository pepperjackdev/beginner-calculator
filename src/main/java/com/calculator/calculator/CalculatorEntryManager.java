package com.calculator.calculator;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.BinaryOperator;

public class CalculatorEntryManager<T> {
    private final Deque<Operation<T, BinaryOperator<T>>> operations;

    public CalculatorEntryManager() {
        this.operations = new LinkedList<>();
    }

    public void add(Operation<T, BinaryOperator<T>> o) {
        if (retrieve() != null && retrieve().getOperator() != null) {

            Operation<T, BinaryOperator<T>> last = operations.peekFirst();

            operations.addFirst(
                    new Operation<>(Objects.requireNonNull(last).getOperatedValue(o.getValue()), o.getOperator())
            );

        } else {
            operations.addFirst(o);
        }
    }

    public Operation<T, BinaryOperator<T>> retrieve() {
        return (!operations.isEmpty()) ? operations.peekFirst() : null;
    }

    public boolean pending() {
        return !operations.isEmpty() && retrieve().getOperator() != null;
    }

    public void clearEntries() {
        operations.clear();
    }

    public void clearLastEntry() {
        operations.pollFirst();
    }

}
