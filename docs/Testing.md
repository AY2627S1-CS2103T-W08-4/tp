---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: [`seedu.address.commons.util.StringUtilTest`](https://github.com/AY2627S1-CS2103T-W08-4/tp/blob/master/src/test/java/seedu/address/commons/util/StringUtilTest.java)
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`


## Selection regression checks

Run `./gradlew check coverage` (Windows: `gradlew.bat check coverage`) for the full
suite. For direct selection, `SelectCommandParserTest`, `SelectCommandTest` and
`StudentSelectionIntegrationTest` verify displayed indices and read-only behavior.
The select methods in `MvpRosterUiTest` cover detail refresh, command focus and
retention of selection/details/input after errors. Use synthetic data and temporary
storage; packaged-app and cross-platform checks remain separate.
