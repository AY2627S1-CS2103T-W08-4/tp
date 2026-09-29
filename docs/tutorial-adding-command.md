# Adding a command tutorial

This branch follows the [SE-EDU Adding a Command tutorial](https://se-education.org/guides/tutorials/ab3AddRemark.html).
The remark command design and implementation approach are adapted from that tutorial, with Codex assistance.

The parser recognises `remark INDEX [r/REMARK]`. The command creates an immutable replacement Person,
updates the model, displays the remark on the contact card, and persists it through JSON storage.
Existing JSON without remarks loads with an empty remark; ordinary edit commands preserve remarks.
Duplicate remark prefixes are rejected consistently with other single-valued AB3 fields.

Validation: `./gradlew check`, including integration tests for adding, replacing, clearing,
filtered indexes, invalid input, edit preservation and JSON round trips.
