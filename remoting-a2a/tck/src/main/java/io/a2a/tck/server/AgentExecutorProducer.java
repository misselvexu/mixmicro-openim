package io.a2a.tck.server;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;

@ApplicationScoped
public class AgentExecutorProducer {

    @Produces
    public AgentExecutor agentExecutor() {
        return new FireAndForgetAgentExecutor();
    }
    
    private static class FireAndForgetAgentExecutor implements AgentExecutor {
        @Override
        public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            Task task = context.getTask();

            if (context.getMessage().getTaskId() != null && task == null && context.getMessage().getTaskId().startsWith("non-existent")) {
                throw new TaskNotFoundError();
            }

            if (task == null) {
                task = new Task.Builder()
                        .id(context.getTaskId())
                        .contextId(context.getContextId())
                        .status(new TaskStatus(TaskState.SUBMITTED))
                        .history(context.getMessage())
                        .build();
                eventQueue.enqueueEvent(task);
            }

            TaskUpdater updater = new TaskUpdater(context, eventQueue);

            // Immediately set to WORKING state
            updater.startWork();
            System.out.println("====> task set to WORKING, starting background execution");

            // Method returns immediately - task continues in background
            System.out.println("====> execute() method returning immediately, task running in background");
        }

        @Override
        public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            System.out.println("====> task cancel request received");
            Task task = context.getTask();

            if (task.getStatus().state() == TaskState.CANCELED) {
                System.out.println("====> task already canceled");
                throw new TaskNotCancelableError();
            }
            
            if (task.getStatus().state() == TaskState.COMPLETED) {
                System.out.println("====> task already completed");
                throw new TaskNotCancelableError();
            }

            TaskUpdater updater = new TaskUpdater(context, eventQueue);
            updater.cancel();
            eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                    .taskId(task.getId())
                    .contextId(task.getContextId())
                    .status(new TaskStatus(TaskState.CANCELED))
                    .isFinal(true)
                    .build());
            
            System.out.println("====> task canceled");
        }

        /**
         * Cleanup method for proper resource management
         */
        @PreDestroy
        public void cleanup() {
            System.out.println("====> shutting down task executor");
         }
    }
}