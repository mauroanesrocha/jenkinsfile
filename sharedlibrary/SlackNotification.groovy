package org.example

import groovy.json.JsonOutput
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.steps.StepContext
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution
import org.jenkinsci.plugins.workflow.steps.Step
import org.jenkinsci.plugins.workflow.steps.StepExecution
import org.jenkinsci.plugins.workflow.steps.StepDescriptor

class SlackNotificationStep extends Step {
    String message
    String slackWebhookUrl

    @DataBoundConstructor
    SlackNotificationStep(String message, String slackWebhookUrl) {
        this.message = message
        this.slackWebhookUrl = slackWebhookUrl
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context)
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "slackNotification"
        }

        @Override
        public String getDisplayName() {
            return "Send Slack Notification"
        }

        @Override
        public Set<Class<?>> getRequiredContext() {
            return new HashSet<>(Arrays.asList(
                TaskListener.class,
                Run.class
            ))
        }
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        transient SlackNotificationStep step

        Execution(SlackNotificationStep step, StepContext context) {
            super(context)
            this.step = step
        }

        @Override
        protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class)
            Run run = getContext().get(Run.class)

            String payload = JsonOutput.toJson([text: step.message])

            sh "curl -X POST -H 'Content-type: application/json' --data '${payload}' ${step.slackWebhookUrl}"

            return null
        }
    }
}

