package sk.koperko.rxjobqueue;

import java.util.concurrent.Callable;

import rx.Observable;

public abstract class Job<T> implements Callable<T> {

    protected int mPriority;

    public Job(int priority) {
        mPriority = priority;
    }

    public abstract T run();

    @Override
    public final T call() {
        return run();
    }

    public Observable<?> retryWhen(Observable<? extends Throwable> errorObservable) {
        return Observable.never();
    }

    public Integer getPriority() {
        return mPriority;
    }
}