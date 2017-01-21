package sk.koperko.rxjobqueue;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;

/**
 * Created by koper on 17.01.17.
 */
public class RxJobQueue<T> {

    private int mConsumerCount;
    private PriorityBlockingQueue<JobQueueItem<T>> mQueue = new PriorityBlockingQueue<>(10, JobQueueItem.COMPARATOR);
    private PublishSubject<ConnectableObservable<T>> mConsumerSubject = PublishSubject.create();
    private Observable<T> mQueueObservable;

    private List<Consumer> mConsumers = new ArrayList<>();
    private Subscription mMainSubscription;

    public RxJobQueue(int consumerCount) {
        mConsumerCount = consumerCount;
        mQueueObservable = mConsumerSubject.flatMap(ConnectableObservable::autoConnect);
    }

    public void start() {
        for (int i = 0; i < mConsumerCount; i++) {
            Consumer<T> thread = new Consumer<>(mQueue, mConsumerSubject);
            mConsumers.add(thread);
            thread.start();
        }
        mMainSubscription = mQueueObservable.subscribe();
    }

    public void stop() {
        for (Thread consumer : mConsumers) {
            consumer.interrupt();
        }
        mConsumers.clear();
        if (!mMainSubscription.isUnsubscribed()) mMainSubscription.unsubscribe();
    }

    public Observable<T> push(Job<T> job) {
        ConnectableObservable<T> observable = Observable.fromCallable(job)
                .retryWhen(job::retryWhen)
                .publish();

        mQueue.add(new JobQueueItem<>(job, observable, System.nanoTime()));
        return observable;
    }


    private static class Consumer<T> extends Thread {

        private PriorityBlockingQueue<JobQueueItem<T>> mQueue;
        private PublishSubject<ConnectableObservable<T>> mSubject;

        Consumer(PriorityBlockingQueue<JobQueueItem<T>> queue, PublishSubject<ConnectableObservable<T>> subject) {
            mQueue = queue;
            mSubject = subject;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    consume(mQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void consume(JobQueueItem<T> item) {
            ConnectableObservable<T> observable = item.mJobObservable;
            mSubject.onNext(observable);
        }
    }

    private static class JobQueueItem<T> {

        static Comparator<JobQueueItem> COMPARATOR = new Comparator<JobQueueItem>() {
            @Override
            public int compare(JobQueueItem job1, JobQueueItem job2) {
                int priorityOrder = job1.mJob.getPriority()
                        .compareTo(job2.mJob.getPriority());
                if (priorityOrder == 0) {
                    return job1.mOrderTimestamp.compareTo(job2.mOrderTimestamp);
                } else {
                    return priorityOrder;
                }

            }
        };

        private Job<T> mJob;
        private ConnectableObservable<T> mJobObservable;
        private Long mOrderTimestamp;

        JobQueueItem(Job<T> job, ConnectableObservable<T> jobObservable, long orderTimestamp) {
            mJob = job;
            mJobObservable = jobObservable;
            mOrderTimestamp = orderTimestamp;
        }
    }


}
