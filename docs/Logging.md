---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* Packaged application logs are stored beside the JAR; unpackaged development
  logs use the working directory. Percent characters in the home-folder path are
  treated literally rather than as `FileHandler` pattern substitutions.
* If the log file cannot be opened, initialization reports the failure through the
  console handler and continues. File logging is optional; its failure must not
  prevent startup or recovery of student data.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).
