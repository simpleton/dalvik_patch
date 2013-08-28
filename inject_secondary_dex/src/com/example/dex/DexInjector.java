/**
 * 
 */
package com.example.dex;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class DexInjector {
	
	/**
	 * TODO:
	 * maybe the system load our injected dex asynchronously. so we should inject our dex as early as possible 
	 * @param app
	 * @param path
	 * @return
	 */
	public static final Boolean inject(Application app, String path) {
		Boolean result;
		if (Build.VERSION.SDK_INT >= 14) {
			result = inject_GE_api14(app, path);
		} else {
			result = inject_BL_api14(app, path);
		}
		return result;
	}
	
	@SuppressLint("NewApi")
	private static Boolean inject_BL_api14(Application app, String path) {
		Boolean result = true;
		PathClassLoader path_classloader = (PathClassLoader) app.getClassLoader();
		DexClassLoader dex_classloader = new DexClassLoader(path, app.getDir(
				"dex", 0).getAbsolutePath(), path, app.getClassLoader());
		try {
			dex_classloader.loadClass("SecondDex");
			setField(path_classloader,
					PathClassLoader.class,
					"mPaths",
					appendArray(getField(path_classloader, PathClassLoader.class, "mPaths"),
								getField(dex_classloader, DexClassLoader.class, "mRawDexPath")));
			
			setField(path_classloader,
					PathClassLoader.class,
					"mFiles",
					combineArray(getField(path_classloader, PathClassLoader.class, "mFiles"),
								 getField(dex_classloader, DexClassLoader.class, "mFiles")));
			
			setField(path_classloader,
					PathClassLoader.class,
					"mZips",
					combineArray(getField(path_classloader, PathClassLoader.class, "mZips"),
								 getField(dex_classloader, DexClassLoader.class, "mZips")));
			
			setField(path_classloader,
					PathClassLoader.class,
					"mDexs",
					combineArray(getField(path_classloader, PathClassLoader.class, "mDexs"),
								 getField(dex_classloader, DexClassLoader.class,	"mDexs")));
			
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	private static Boolean inject_GE_api14(Application app, String libPath) {
		PathClassLoader pathClassLoader = (PathClassLoader) app
				.getClassLoader();
		DexClassLoader dexClassLoader = new DexClassLoader(libPath, 
															app.getDir("dex", 0).getAbsolutePath(), 
															libPath, app.getClassLoader());
		Boolean result = true;
		try {
			
			Object dexElements = combineArray(
					getDexElements(getPathList(pathClassLoader)),
					getDexElements(getPathList(dexClassLoader)));
			
			Object pathList = getPathList(pathClassLoader);
			setField(pathList, pathList.getClass(), "dexElements", dexElements);
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
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

	private static void setField(Object obj, Class<?> cl, String field,
			Object value) throws NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
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
