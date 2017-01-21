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

### Todo list
- [ ] serializable jobs
- [ ] job dependencies
- [ ] waiting for network
- [ ] switch to RxJava 2.x
- [ ] design consumers that take advantage of rx 'Scheduler' and allow users to specify
