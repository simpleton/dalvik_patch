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