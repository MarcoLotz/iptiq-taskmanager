# Coding Challenge: Task Manager

## Overview

## Getting started
It's the first time that I am using <b>GitHub Packages</b> as artifact store.
It seems that maven requires special configuration on settings.xml in order to retrieve packages from GitHub Repository.

In order to build and perform tests, please run

```shell
mvn clean verify
```

Should an error pointing that 'marcolotz-parent cannot be found' prompt, please retrieve it from [here|https://github.com/marcolotz/parent].
After retrieving it,  install the parent pom version 1.0.0 in your local maven repository with:

```shell
mvn clean install
```

## Implementation decisions

This is my favourite part :)

To start the discussion, I decided not to create some web-server or show-off with OpenAPI 3.0 auto-generated client/controllers because the challenge didn't request it.
In reality, it requested KISS and YANGNI - so I tried to keep the more simple I could.

Clearly the author of the challenge is experienced in the Software Architecture concepts, when requesting for "default behaviour".

## Interface Segregation
I used Hexagonal Architecture for this project.
There are two ports, declaring interfaces for the main components of the software artifact.
The implementation of those components is done on the core package.
I personally do not like to use the convention InterfaceNameImpl naming to implementation classes.
Since Java libraries dont use that (there's no ListImpl but a specification detail of the implementation e.g. ArrayList).
I used the same criteria when implementing the interfaces.

## Dependency Injection
Due to the Interface Segregation, one can inject any implementation of Task Manager and it should work out of the box.
I tested to make sure that Liskov's Substitution Principle holds on all cases.
As one can see, the tests extend a base test.
Unless for behaviours that we intentionally changed in the subclass, all classes are also tested against expected original behaviours.

As time goes, one also learn to never have time manipulation using sleep or any other idle work inside tests.
Dependency Injection and Interface Segregation comes really in hand when I provided a test only implementation of TimeProvider - making sure that all events happen in sequence and in distinct seconds without requiring sleep() calls.

## Inheritance
The problem description screamed for code duplication reduction, using specialized methods only when required.
And that's what I done - SimpleTaskManager is the base class.
Both FifoTaskManager and PriorityQueueTaskManager differ only by what storage data structure it uses and the implementation of addProcess() behaviour.
Everything else is the same and work also the same.

No code duplication.
Even on tests.

## Synchronized

Write operations on the task manager should be thread safe.
I have done so by having all methods that perform mutations on Task Manager state being annotate with @Synchronized.
Of course, other strategies could be used here (e.g. Using a concurrent data structure).

## Auto PID
I assumed that PID is auto-generated at the construction of a Process.
This is just an implementation decision.
It could very well have been provided at concrete instantiation time too, leaving the creation of unique IDs to some other component.

## Single Responsibility
Using Comparator on SortingMethod empowers decoupling between the TaskManager and how sorting should be done.
I think there's more cohesion like this, and feels like it was a correct decision.

TaskManager should be responsible for ManagingTasks - not on how sorting should be implemented.

## Decorator Pattern
I have used Decorator Pattern to model Processes.
They are, as expected, immutable. And they are meaningful just by having pid and priority.
Nothing more on the domain model.

However, in order to be able to order process by ingestion time, Process requires information of when it was ingested.
In some data structures - e.g. LinkedLists - this information is implicit.
On some others (e.g. Priority Queues) this information can easily be lost.

In order to solve this problem, I created a decorator for Process called AcceptedProcess that contains both the Process and its ingestion time.
All good - Process contains only data relevant for the process. The Decorator takes care of extra behaviours and information.

## Async Logging
Just for fun I used async / lazy logging (Log4j2).
I've been working with lots of Kafka lately and Async logging really makes a difference on high-throughput systems.
Kafka source currently still uses synchronous logging - I saw a PR being open this week to update to Log4j2.

# Optimisations

## Optimized Priority Task Manager
Priority Queues are Heaps.
They are used for Max/Min calculations and have runtimes of O(log n) insert and O(log n) removals.
The upside of this data structure is that you can in O(1) access the Max or Min.

Knowing that, I wrote PriorityOptimizedTaskManager that uses some nested maps in order to reduce the run time complexity of some operations.

- When Task manager is not full, inserts are O(1) (insertion on maps).
- When Task manager is full, inserts are O(1) too (removal from LinkedHashMap and insert of Map).
- When killing a specific process by pid, search is also O(1).
- When listing the process by priority, the result is constructed in O(n) time - no sorting required.
- In all other cases, it performs as good as the other TaskManagers.

In order to implement this, of course, I had to loose a bit the generation of the class, since most of the interface methods had to be re-implemented.

## Priority Types
Some people could say that this is open/close principle - but I rather not because open/close is more related with object oriented design and classes extensions.
Having said that, when I designed the Priority Enum class I decided to no make it sequential (e.g. prio 0, 1, 2) but with intervals (e.g. 0, 500, 1000).
By my personal experience, in the future always appears a requirement for a "MiddleHigh" Priority that needs to be put in between.
And sequential enums wouldn't handle that.