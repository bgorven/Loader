A utility for loading distributed platform native libraries, extracted from [sigar](https://github.com/hyperic/sigar/tree/master/bindings/java/hyperic_jni/src/org/hyperic/jni)

Makes a best-effort attempt to detect the OS and Architecture your code is being run on, and extract and load the appropriate binary, if found in your classpath.

Usage:

Simply call ArchLoader.load(Class<?> clazz), passing in the class containing native methods to be loaded.

```
package com.example.hello;

import org.bven.jni.*;

public class Native {
    static {
        //Same effect as ArchLoader.load("Native", "lib" + File.separator + "com.example.hello", Native.class.getClassLoader());
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
        │   └── libNative.so
        ├── linux-x64
        │   └── libNative.so
        ├── linux-x86
        │   └── libNative.so
        ├── windows-arm
        │   └── Native.dll
        ├── windows-x64
        │   └── Native.dll
        └── windows-x86
            └── Native.dll
```