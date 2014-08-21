/**
 * 
 */
package com.example.dex;

import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class DexInjector {

    private static final String TAG = DexInjector.class.getSimpleName();
    /**
     * inject your dex file to PathClassLoader
     * @param dexPath  the list of jar/apk files containing classes and resources, delimited by File.pathSeparator, which defaults to ":" on Android
     * @param defaultDexOptPath  directory where optimized dex files should be written; must <b>not</b> be null
     * @param nativeLibPath  the list of directories containing native libraries, delimited by File.pathSeparator; may be null
     * @return whether success
     */
    public static synchronized Boolean inject(String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        try {
            Class.forName("dalvik.system.LexClassLoader");
            return injectInAliyunOs(dexPath, defaultDexOptPath, nativeLibPath, dummyClassName);
        } catch (ClassNotFoundException e) {
        }

        boolean hasBaseDexClassLoader = true;

        try {
            Class.forName("dalvik.system.BaseDexClassLoader");
        } catch (ClassNotFoundException e) {
            hasBaseDexClassLoader = false;
        }

        if (!hasBaseDexClassLoader) {
            return injectBelowApiLevel14(dexPath, defaultDexOptPath, nativeLibPath, dummyClassName);
        } else {
            return injectAboveEqualApiLevel14(dexPath, defaultDexOptPath, nativeLibPath, dummyClassName);
        }
    }

    private static synchronized Boolean injectInAliyunOs(
            String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        Log.i(TAG, "-->injectInAliyunOs");
        PathClassLoader localClassLoader = (PathClassLoader) DexInjector.class.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, nativeLibPath, localClassLoader);
        String lexFileName = new File(dexPath).getName();
        lexFileName = lexFileName.replaceAll("\\.[a-zA-Z0-9]+", ".lex");
        try {
            dexClassLoader.loadClass(dummyClassName);
            Class<?> classLexClassLoader = Class.forName("dalvik.system.LexClassLoader");
            Constructor<?> constructorLexClassLoader = classLexClassLoader.getConstructor(
                    String.class, String.class, String.class, ClassLoader.class);
            Object localLexClassLoader = constructorLexClassLoader.newInstance(
                    defaultDexOptPath + File.separator + lexFileName,
                    defaultDexOptPath,
                    nativeLibPath,
                    localClassLoader);
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mPaths",
                    appendArray(
                            getField(localClassLoader, PathClassLoader.class, "mPaths"),
                            getField(localLexClassLoader, classLexClassLoader, "mRawDexPath")));
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mFiles",
                    combineArray(
                            getField(localClassLoader, PathClassLoader.class, "mFiles"),
                            getField(localLexClassLoader, classLexClassLoader,"mFiles")));
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mZips",
                    combineArray(
                            getField(localClassLoader, PathClassLoader.class, "mZips"),
                            getField(localLexClassLoader, classLexClassLoader, "mZips")));
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mLexs",
                    combineArray(
                            getField(localClassLoader, PathClassLoader.class, "mLexs"),
                            getField(localLexClassLoader, classLexClassLoader, "mDexs")));

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        Log.i(TAG, "<--injectInAliyunOs end.");
        return true;
    }

    private static synchronized Boolean injectBelowApiLevel14(
            String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        Log.i(TAG, "--> injectBelowApiLevel14");
        PathClassLoader pathClassLoader = (PathClassLoader) DexInjector.class.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, nativeLibPath, pathClassLoader);
        try {
            dexClassLoader.loadClass(dummyClassName);
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mPaths",
                    appendArray(
                            getField(pathClassLoader, PathClassLoader.class,"mPaths"),
                            getField(dexClassLoader, DexClassLoader.class,"mRawDexPath")));
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mFiles",
                    combineArray(
                            getField(pathClassLoader, PathClassLoader.class, "mFiles"),
                            getField(dexClassLoader, DexClassLoader.class, "mFiles")));
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mZips",
                    combineArray(
                            getField(pathClassLoader, PathClassLoader.class, "mZips"),
                            getField(dexClassLoader, DexClassLoader.class, "mZips")));
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mDexs",
                    combineArray(
                            getField(pathClassLoader, PathClassLoader.class, "mDexs"),
                            getField(dexClassLoader, DexClassLoader.class, "mDexs")));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        Log.i(TAG, "<-- injectBelowApiLevel14");
        return true;
    }

    private static synchronized Boolean injectAboveEqualApiLevel14(
            String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        Log.i(TAG, "--> injectAboveEqualApiLevel14");
        PathClassLoader pathClassLoader = (PathClassLoader) DexInjector.class.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, nativeLibPath, pathClassLoader);
        try {
            dexClassLoader.loadClass(dummyClassName);
            Object dexElements = combineArray(
                    getDexElements(getPathList(pathClassLoader)),
                    getDexElements(getPathList(dexClassLoader)));

            Object pathList = getPathList(pathClassLoader);
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        Log.i(TAG, "<-- injectAboveEqualApiLevel14 End.");
        return true;
    }


    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }

    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class<?> cl, String field, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

    private static Object appendArray(Object array, Object value) {
        Class<?> localClass = array.getClass().getComponentType();
        int i = Array.getLength(array);
        int j = i + 1;
        Object localObject = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(localObject, k, Array.get(array, k));
            } else {
                Array.set(localObject, k, value);
            }
        }
        return localObject;
    }
}
