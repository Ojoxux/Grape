#!/bin/bash
# 1) Gradle continuous build: watches src/ and recompiles .class files on change
./gradlew classes --continuous --no-daemon &

# 2) bootRun: DevTools detects .class changes and auto-restarts
./gradlew bootRun --no-daemon
