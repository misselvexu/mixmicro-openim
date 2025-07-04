package io.a2a.server.apps.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.UnsupportedOperationError;
import io.quarkus.arc.profile.IfBuildProfile;

@ApplicationScoped
@IfBuildProfile("test")
public class AgentExecutorProducer {

    @Produces
    public AgentExecutor agentExecutor() {
        return new AgentExecutor() {
            @Override
            public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                if (context.getTaskId().equals("task-not-supported-123")) {
                    eventQueue.enqueueEvent(new UnsupportedOperationError());
                }
                eventQueue.enqueueEvent(context.getMessage() != null ? context.getMessage() : context.getTask());
            }

            @Override
            public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                if (context.getTask().getId().equals("cancel-task-123")) {
                    TaskUpdater taskUpdater = new TaskUpdater(context, eventQueue);
                    taskUpdater.cancel();
                } else if (context.getTask().getId().equals("cancel-task-not-supported-123")) {
                    throw new UnsupportedOperationError();
                }
            }
        };
    }
}
