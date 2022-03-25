# Cucumber-Performance

A concurrent behavior driven testing(CBDT) tool and performance testing framework for Cucumber IO.

## What is Cucumber Perf?
Cucumber-Performance is a tool to simulate concurrent user behavior using cucumber features as runner specification.

### What is Cucumber?
Cucumber is a implementation of [Behavior Driven Development](https://en.wikipedia.org/wiki/Behavior-driven_development) [(BDD)](https://cucumber.io/docs/bdd/).
Which uses simple natural language scripts to define a software feature.
These executable specifications are written in a language called [Gherkin](https://cucumber.io/docs/gherkin/).
Example:
```
#beer.feature
Feature: Beer
  Scenario: Jeff dinks a beer
	  Given: Jeff is of age and has a beer
	  And: Jeff opens his beer.
	  When: Jeff takes a sip.
	  Then: Verify he enjoyed it.
```

These scripts can be used to develop the features themselves but also drive [automated tests](https://cucumber.io/docs/guides/10-minute-tutorial)

### The issue?
So, you now have a working functional automation test suite.
But you want to run a performance test. Generally this would require either rewriting your existing functional tests or copying a bunch of code.
Also, you would need to create or implement a performance test harness.

Most likely each team will end up with something that is project specific and doesn't use the existing functional code base.

### The fix?
Cucumber Performance provides a level of automation on top of Cucumber.
And is an implementation of a new concept (as far as I know) called Concurrent Behavior Driven Testing (CBDT).

## What is Concurrent Behavior Driven Testing?
Concurrent Behavior Driven Testing (CBDT) is the concept of using BDD features to simulate real world concurrent events. 

Most systems have multiple concurrent users who may be using different but complementary features, which been previously defined in [Gherkin](https://cucumber.io/docs/gherkin/).
CBDT allows you to document these real world situations in a simple human readable domain-specific scripting language.

CBDT requires an automation team to follow strict guidelines when coding functional test cases.
Being careful to avoid static variables and race conditions that will cause failures in a multiple-threaded world.
This of course requires a larger understanding of programming or at least team leadership that can enforce these guidelines.

### How does Cucumber Performance work?
Cucumber Performance provides a means to use your existing functional tests without writing a single line of code.
It provides the ability to run performance simulations with support for common load testing features:
* Timed Tests
* Multi-Threading
* Thread Count Limits
* Ramp Up/Down
* Data replacing
* Random Wait
* Statistics
* Console reporting
And creates a number of outputs
* Data Points (csv)
* JUnit Report
* Logging
* Summary Report
* Taurus Final Stats

It uses a new type of script called Salad.
Salad is a re-implementation of Cucumber Gherkin with the focus on performance simulations.

```
Plan: Bar visit

Simulation: Jeff drinks 3 beers.
  Group: beer.feature
  Runners: 1
  Count: 3
```
## Plans:
Here is an example plan
```
Plan: test
Simulation: simulation 1
Group test.feature
	#slices
	#these values will replace property "value out"
	|value out|
	|changed value 1|
	|changed value 2|
	#number of threads
	Runners: 2
	#total number of threads to run.
	Count: 2
#a optional random wait mean for before thread runs tests.
#thread will wait between +-50% of this mean
RandomWait: 00:00:02

#Will run all groups for the period below
Simulation Period: simulation 2 period
Group test.feature
	|value out|
	|changed value |
		Threads: 5
		#count is ignored in a simulation period
		Count: 1
#run time
Time: 00:00:30
RampUp: 00:00:10
RampDown: 00:00:10
```

## Getting Started
It takes some planning to implement Cucumber Perf.

Your functional automation should follow these rules:
* Use a non specific test harness. This should standardize all your common functions.
* Do not use static variables!
* Properly comment your features and scenarios. You want to keep track of what scenarios can be run multithreaded.

Follow directions in [wiki](https://github.com/mpinardi/cucumber-performance/wiki) to get up and running.

### Installing
Maven
> Note currently Cucumber versions 6.* and 7.* are not supported but hopefully will be.

Cucumber 5.*
```
<dependency>
  <groupId>com.github.mpinardi</groupId>
  <artifactId>cucumber-perf</artifactId>
  <version>4.0.3</version>
</dependency>
```

Cucumber 4.*
```
<dependency>
  <groupId>com.github.mpinardi</groupId>
  <artifactId>cucumber-perf</artifactId>
  <version>3.0.2</version>
</dependency>
```
## Versioning
We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
