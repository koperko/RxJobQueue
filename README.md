# RxJobQueue
An attempt to implement priority queue for job scheduling using reactive extensions in Java. 

### Motivation
- deepening my RxJava knowledge, mainly creating hot observables using `ConnectableObservable` type
- exposing the result of scheduled jobs as observables to take advantage of the benefits that RxJava offers for the asynchronous nature of job scheduling
- brushing up my producer -> consumer pattern experience :-)

### Advantages
- base stream of all events allows reacting to more complex scenarios
- combining result observables of multiple jobs
- error handling using retryWhen operator

## Usage

### Setup the queue
```java
int numberOfParallelConsumers = 2;

//Replace BaseReturnType with your base type which every job result must extend
RxJobQueue<BaseReturnType> queue = new RxJobQueue<>(numberOfParallelConsumers);
```

### Create a job
```java
public class MyJob extends Job<MyResultType> {
    public MyJob(int priority){
        super(priority);
        // Lower int means higher priority
    }
    
    @Override
    public MyResultType run(){
        MyResultType result;
        // do some work here
        return result;
    }
    
    //Optional ... Default implementation doesn't use retry logic and continues with next job in queue
    @Override
    public Observable<?> retryWhen(Observable<? extends Throwable> errorObservable){
        // This is the function that is passed to retryWhen operator. For more information, check out the reactive extensions documentation (link below)
        return errorObservable;
    }
    
}
```
[RetryWhen](http://reactivex.io/documentation/operators/retry.html) operator documentation

### Push job instances
```java
Observable<MyResultType> resultObservable = queue.push(new MyJob(1));
// Note: This subscription does not fire the job execution. It is hot observable and just observes for job result. Management of its execution relies solely on the queue itself.
result.subscribe((result) -> {
   // Yaay! We have our result here ..  
});

queue.start();

...

queue.stop();
```
You can push jobs to the queue before or after `start()` method has been called. The queue just accumulates items until you call `start()` which initializes consumers.

--

### Todo list
- [ ] serializable jobs
- [ ] tests
- [ ] job dependencies
- [ ] waiting for network
- [ ] switch to RxJava 2.x
- [ ] design consumers that take advantage of rx 'Scheduler' and allow users to specify
- [ ] better consumer management
