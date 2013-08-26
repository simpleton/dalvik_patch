/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 This is a sample project to demonstrate the partitioning of dex
 files and runtime class loading in Dalvik.  Since the project requires a
 modified build process, it cannot be built with standard tool like
 ADT Eclipse plug-in.  Instead, use the included Ant build script.

 BUILD INSTRUCTIONS (verified on Android SDK Tools Rev. 16)

 1) Ensure Ant is properly set up by referring to:
    http://d.android.com/guide/developing/building/building-cmdline.html

 2) In local.properties, update the "sdk.dir" value to your local Android SDK home directory.

 3) In project.properties, ensure the proper build target is used.

 4) The build.xml file included in this project contains a modified "-dex" target,
    which has the logic to create two dex files - standard and secondary.
    The "-dex" target is automatically invoked when either "release" or "debug"
    target is invoked.

 5) To make a debug build, execute: ant debug
    To make a release build, ensure that key.store is properly configured.
    Then execute: ant release
 Custom Class Loading in Dalvik

[This post is by Fred Chung, who’s an Android Developer Advocate — Tim Bray]

The Dalvik VM provides facilities for developers to perform custom class loading. Instead of loading Dalvik executable (“dex”) files from the default location, an application can load them from alternative locations such as internal storage or over the network.

This technique is not for every application; In fact, most do just fine without it. However, there are situations where custom class loading can come in handy. Here are a couple of scenarios:

    Big apps can contain more than 64K method references, which is the maximum number of supported in a dex file. To get around this limitation, developers can partition part of the program into multiple secondary dex files, and load them at runtime.

    Frameworks can be designed to make their execution logic extensible by dynamic code loading at runtime.

We have created a sample app to demonstrate the partitioning of dex files and runtime class loading. (Note that for reasons discussed below, the app cannot be built with the ADT Eclipse plug-in. Instead, use the included Ant build script. See Readme.txt for detail.)

The app has a simple Activity that invokes a library component to display a Toast. The Activity and its resources are kept in the default dex, whereas the library code is stored in a secondary dex bundled in the APK. This requires a modified build process, which is shown below in detail.

Before the library method can be invoked, the app has to first explicitly load the secondary dex file. Let’s take a look at the relevant moving parts.
Code Organization

The application consists of 3 classes.

    com.example.dex.MainActivity: UI component from which the library is invoked

    com.example.dex.LibraryInterface: Interface definition for the library

    com.example.dex.lib.LibraryProvider: Implementation of the library

The library is packaged in a secondary dex, while the rest of the classes are included in the default (primary) dex file. The “Build process” section below illustrates how to accomplish this. Of course, the packaging decision is dependent on the particular scenario a developer is dealing with.
Class loading and method invocation

The secondary dex file, containing LibraryProvider, is stored as an application asset. First, it has to be copied to a storage location whose path can be supplied to the class loader. The sample app uses the app’s private internal storage area for this purpose. (Technically, external storage would also work, but one has to consider the security implications of keeping application binaries there.)

Below is a snippet from MainActivity where standard file I/O is used to accomplish the copying.

  // Before the secondary dex file can be processed by the DexClassLoader,
  // it has to be first copied from asset resource to a storage location.
  File dexInternalStoragePath = new File(getDir("dex", Context.MODE_PRIVATE),
          SECONDARY_DEX_NAME);
  ...
  BufferedInputStream bis = null;
  OutputStream dexWriter = null;

  static final int BUF_SIZE = 8 * 1024;
  try {
      bis = new BufferedInputStream(getAssets().open(SECONDARY_DEX_NAME));
      dexWriter = new BufferedOutputStream(
          new FileOutputStream(dexInternalStoragePath));
      byte[] buf = new byte[BUF_SIZE];
      int len;
      while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
          dexWriter.write(buf, 0, len);
      }
      dexWriter.close();
      bis.close();
      
  } catch (. . .) {...}

Next, a DexClassLoader is instantiated to load the library from the extracted secondary dex file. There are a couple of ways to invoke methods on classes loaded in this manner. In this sample, the class instance is cast to an interface through which the method is called directly.

Another approach is to invoke methods using the reflection API. The advantage of using reflection is that it doesn’t require the secondary dex file to implement any particular interfaces. However, one should be aware that reflection is verbose and slow.

  // Internal storage where the DexClassLoader writes the optimized dex file to
  final File optimizedDexOutputPath = getDir("outdex", Context.MODE_PRIVATE);

  DexClassLoader cl = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
                                         optimizedDexOutputPath.getAbsolutePath(),
                                         null,
                                         getClassLoader());
  Class libProviderClazz = null;
  try {
      // Load the library.
      libProviderClazz =
          cl.loadClass("com.example.dex.lib.LibraryProvider");
      // Cast the return object to the library interface so that the
      // caller can directly invoke methods in the interface.
      // Alternatively, the caller can invoke methods through reflection,
      // which is more verbose. 
      LibraryInterface lib = (LibraryInterface) libProviderClazz.newInstance();
      lib.showAwesomeToast(this, "hello");
  } catch (Exception e) { ... }

Build Process

In order to churn out two separate dex files, we need to tweak the standard build process. To do the trick, we simply modify the “-dex” target in the project’s Ant build.xml.

The modified “-dex” target performs the following operations:

    Create two staging directories to store .class files to be converted to the default dex and the secondary dex.

    Selectively copy .class files from PROJECT_ROOT/bin/classes to the two staging directories.

          <!-- Primary dex to include everything but the concrete library
                     implementation. -->
                <copy todir="${out.classes.absolute.dir}.1" >
                    <fileset dir="${out.classes.absolute.dir}" >
                            <exclude name="com/example/dex/lib/**" />
                    </fileset>
                </copy>
                <!-- Secondary dex to include the concrete library implementation. -->
                <copy todir="${out.classes.absolute.dir}.2" >
                    <fileset dir="${out.classes.absolute.dir}" >
                            <include name="com/example/dex/lib/**" />
                    </fileset>
                </copy>     

    Convert .class files from the two staging directories into two separate dex files.

    Add the secondary dex file to a jar file, which is the expected input format for the DexClassLoader. Lastly, store the jar file in the “assets” directory of the project.

        <!-- Package the output in the assets directory of the apk. -->
                <jar destfile="${asset.absolute.dir}/secondary_dex.jar"
                       basedir="${out.absolute.dir}/secondary_dex_dir"
                       includes="classes.dex" />

To kick-off the build, you execute ant debug (or release) from the project root directory.

That’s it! In the right situations, dynamic class loading can be quite useful.
