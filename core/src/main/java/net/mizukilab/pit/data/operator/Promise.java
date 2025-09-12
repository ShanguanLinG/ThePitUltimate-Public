package net.mizukilab.pit.data.operator;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import nya.Skip;

@Skip
public class Promise extends ObjectArraySet<Runnable> {

    boolean done = false;

    void ret() {
        synchronized (this) {
            done = true;
            this.notifyAll();
        }
        this.forEach(Runnable::run);
    }

    public Promise promise(Runnable run) {
        synchronized (this) {
            if (done) {
                run.run();
                return this;
            }
        }
        this.add(run);
        return this;
    }

    public Promise join() {
        try {
            synchronized (this) {
                if (done) {
                    return this;
                }
                this.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isDone() {
        return done;
    }
}
