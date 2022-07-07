# Compact set

This is tiny implementation of set data structure, which supports smaller set of operations (only `add` and `contains`).
This implementation attempts to be more memory efficient than the standard Java HashSet, 
but may have poorer performance in some cases.

## Use

Library is not published to Maven central. To use it you need to download sources and build it locally.
Luckily, it's not too difficult.

Pull this git repository and run in the root directory:

```bash
./gradlew publishToMavenLocal
```