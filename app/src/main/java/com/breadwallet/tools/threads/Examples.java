package com.breadwallet.tools.threads;

public class Examples {


    /*
 * Using it for Background Tasks
 */
    public void doSomeBackgroundWork() {
        BRExecutor.getInstance().forBackgroundTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        // do some background work here.
                    }
                });
    }

    /*
    * Using it for Light-Weight Background Tasks
    */
    public void doSomeLightWeightBackgroundWork() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        // do some light-weight background work here.
                    }
                });
    }

    /*
    * Using it for MainThread Tasks
    */
    public void doSomeMainThreadWork() {
        BRExecutor.getInstance().forMainThreadTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        // do some Main Thread work here.
                    }
                });
    }

    /*
* do some task at high priority
*/
    public void doSomeTaskAtHighPriority(){
        BRExecutor.getInstance().forBackgroundTasks()
                .submit(new PriorityRunnable(Priority.HIGH) {
                    @Override
                    public void run() {
                        // do some background work here at high priority.
                    }
                });
    }
}
