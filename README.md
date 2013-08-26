dalvik_patch
============

## Under the Hood: Dalvik patch for Facebook for Android Implemention##

By David Reiss on Monday, March 4, 2013 at 1:59pm

Facebook is one of the most feature-rich apps available for Android. With features like push notifications, news feed, and an embedded version of Facebook Messenger (a complete app in its own right) all working together in real-time, the complexity and volume of code creates technical challenges that few, if any, other Android developers face--especially on older versions of the platform. (Our latest apps support Android versions as old as Froyo--Android version 2.2--which is almost three years old.) 

 

One of these challenges is related to the way Android's runtime engine, the Dalvik Virtual Machine, handles Java methods. Late last year we completed a  major rebuild of our Android app (https://www.facebook.com/notes/facebook-engineering/under-the-hood-rebuilding-facebook-for-android/10151189598933920), which involved moving a lot of our code from JavaScript to Java, as well as using newer abstractions that encouraged large numbers of small methods (generally considered a good programming practice). Unfortunately, this caused the number of Java methods in our app to drastically increase.   

 

As we were testing, the problem first showed up as described in this bug (http://code.google.com/p/android/issues/detail?id=22586) , which caused our app installation to fail on older Android phones. During standard installation, a program called "dexopt" runs to prepare your app for the specific phone it's being installed on. Dexopt uses a fixed-size buffer (called the "LinearAlloc" buffer) to store information about all of the methods in your app. Recent versions of Android use an 8 or 16 MB buffer, but Froyo and Gingerbread (versions 2.2 and 2.3) only have 5 MB. Because older versions of Android have a relatively small buffer, our large number of methods was exceeding the buffer size and causing dexopt to crash. 

 

After a bit of panic, we realized that we could work around this problem by breaking our app into multiple dex files, using the technique described here (http://android-developers.blogspot.com/2011/07/custom-class-loading-in-dalvik.html), which focuses on using secondary dex files for extension modules, not core parts of the app. 

 

However, there was no way we could break our app up this way--too many of our classes are accessed directly by the Android framework. Instead, we needed to inject our secondary dex files directly into the system class loader. This isn't normally possible, but we examined the Android source code and used Java reflection to directly modify some of its internal structures. We were certainly glad and grateful that Android is open source—otherwise, this change wouldn’t have been possible. 

 

But as we came closer to launching our redesigned app, we ran into another problem. The LinearAlloc buffer doesn't just exist in dexopt--it exists within every running Android program. While dexopt uses LinearAlloc to to store information about all of the methods in your dex file, the running app only needs it for methods in classes that you are actually using. Unfortunately, we were now using too many methods for Android versions up to Gingerbread, and our app was crashing shortly after startup.   

 

There was no way to work around this with dex files since all of our classes were being loaded into one process, and we weren’t able to find any information about anyone who had faced this problem before (since it is only possible once you are already using multiple dex files, which is a difficult technique in itself).  We were on our own. 

 

We tried various techniques to reclaim space, including aggressive use of ProGuard and source code transformations to reduce our method count. We even built a profiler for LinearAlloc usage to figure out what the biggest consumers were. Nothing we tried had a significant impact, and we still needed to write many more methods to support all of the rich content types in our new and improved news feed and timeline.   

 

As it stood, the release of the much-anticipated Facebook for Android 2.0 was at risk. It seemed like we would have to choose between cutting significant features from the app or only shipping our new version to the newest Android phones (ICS and up). Neither seemed acceptable. We needed a better solution.  

 

Once again, we looked to the Android source code. Looking at the definition of the LinearAlloc buffer (https://github.com/android/platform_dalvik/blob/android-2.3.7_r1/vm/LinearAlloc.h#L33), we realized that if we could only increase that buffer from 5 MB to 8 MB, we would be safe! 

 

That's when we had the idea of using a JNI extension to replace the existing buffer with a larger one. At first, this idea seemed completely insane. Modifying the internals of the Java class loader is one thing, but modifying the internals of the Dalvik VM while it was running our code is incredibly dangerous. But as we pored over the code, analyzing all the uses of LinearAlloc, we began to realize that it should be safe as long as we did it at the start of our program. All we had to do was find the LinearAllocHdr object, lock it, and replace the buffer.

 

Finding it turned out to be the hard part. Here’s where it’s stored (https://github.com/android/platform_dalvik/blob/android-2.3.7_r1/vm/Globals.h#L519), buried within the DvmGlobals object, over 700 bytes from the start. Searching the entire object would be risky at best, but fortunately, we had an anchor point: the vmList object just a few bytes before. This contained a value that we could compare to the JavaVM pointer available through JNI.

 

The plan was finally coming together: find the proper value for vmList, scan the DvmGlobals object to find a match, jump a few more bytes to the LinearAlloc header, and replace the buffer. So we built the JNI extension, embedded it in our app, started it up, and...we saw the app running on a Gingerbread phone for the first time in weeks.The plan had worked. 

 

But for some reason it failed on the Samsung Galaxy S II...

The most popular Gingerbread phone...

Of all time...

 

It seems that Samsung made a small change to Android that was confusing our code. Other manufacturers might have done the same, so we realized we needed to make our code more robust. 

 

Manual inspection of the GSII revealed that the LinearAlloc buffer was only 4 bytes from where we expected it, so we adjusted our code to look a few bytes to each side if it failed to find the LinearAlloc buffer in the expected location. This required us to parse our process's memory map to ensure we didn't make any invalid memory references (which would crash the app immediately) and also build some strong heuristics to make sure we would recognize the LinearAlloc buffer when we found it. As a last resort, we found a (mostly) safe way to scan the entire process heap to search for the buffer. 

 

Now we had a version of the code that worked on a few popular phones--but we needed more than just a few. So we bundled our code up into a test app that would run the same procedure we were using for the Facebook app, then just display a large green or red box, indicating success or failure. 

 

We used manual testing, DeviceAnywhere, and a test lab that Google let us borrow to run our test app on 70 different phone models, and fortunately, it worked on every single one!

 

We released this code with Facebook for Android 2.0 in December. It's now running on hundreds of different phone models, and we have yet to find one where it doesn't work. The great speed improvements in that release would not have been possible without this crazy hack. And needless to say, without Android’s open platform, we wouldn’t have had the opportunity to ship our best version of the app. There’s a lot of opportunity for building on Android, and we’re excited to keep bringing the Facebook experience to more people and devices.  
