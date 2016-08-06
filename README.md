[![](https://jitpack.io/v/bgorven/Loader.svg)](https://jitpack.io/#bgorven/Loader)

A utility for loading distributed platform native libraries, extracted from [sigar](https://github.com/hyperic/sigar/tree/master/bindings/java/hyperic_jni/src/org/hyperic/jni)

Makes a best-effort attempt to detect the OS and Architecture your code is being run on, and extract and load the appropriate binary, if found in your classpath.

Adding it to your build:

Details at JitPack https://jitpack.io/#bgorven/Loader

Usage:

Simply call ArchLoader.load(Class<?> clazz), passing in the class containing native methods to be loaded.

```
package com.example.hello;

import org.bven.jni.*;

public class Native {
    static {
        //Same effect as ArchLoader.load("native", "lib" + File.separator + "com.example.hello", Native.class);
        ArchLoader.load(Native.class);
	}

    native void method();
}
```

Your classpath should contain file structure like the following (with binaries for each platform you support).
Full list of os and arch names and versions can be found in ArchName.java

```
src/main/resources/
└── lib
    └── com.example.hello
        ├── freebsd-10-x64
        │   └── libnative.so
        ├── linux-x64
        │   └── libnative.so
        ├── linux-x86
        │   └── libnative.so
        ├── osx-x64
        │   └── libnative.dylib
        ├── osx-x86
        │   └── libnative.dylib
        ├── windows-arm
        │   └── native.dll
        ├── windows-x64
        │   └── native.dll
        └── windows-x86
            └── native.dll
```
