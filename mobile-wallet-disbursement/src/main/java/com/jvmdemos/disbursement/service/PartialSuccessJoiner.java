package com.jvmdemos.disbursement.service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.StructuredTaskScope;

/**
 * A custom Joiner that implements the Saga/Partial Success pattern.
 */
public class PartialSuccessJoiner<T> implements StructuredTaskScope.Joiner<T, List<StructuredTaskScope.Subtask<T>>> {

    private final ConcurrentLinkedQueue<StructuredTaskScope.Subtask<T>> completedTasks = new ConcurrentLinkedQueue<>();

    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends T> subtask) {
        // Invoked when a subtask finishes.
        completedTasks.add((StructuredTaskScope.Subtask<T>) subtask);

        // You return true from this method to cancel the scope (e.g., if a critical subtask fails), or false to keep running.
        // We return false because we want to collect ALL successes and failures.
        return false;
    }

    @Override
    public List<StructuredTaskScope.Subtask<T>> result() {
        // Invoked by join() to produce the final return value for the scope owner, or to throw a specific exception.
        return completedTasks.stream().toList();
    }
}
