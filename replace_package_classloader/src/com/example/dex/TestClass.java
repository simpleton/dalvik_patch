package com.example.dex;

import android.content.Context;
import android.util.Log;

import com.example.dex.lib.LibraryProvider;

public class TestClass {
	private static final String TAG = "TestClass";
	static {
		Log.e(TAG, "TestClass init");
	}
	public static void test(Context ct) {
		try {
			Class.forName("com.example.dex.lib.LibraryProvider");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//LibraryProvider lib = new LibraryProvider();
		Class<?> clazz = null;
		try {
			clazz = Class.forName("com.example.dex.lib.LibraryProvider");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LibraryProvider lib;
		try {
			if (clazz != null) {
				lib = (LibraryProvider) clazz.newInstance();
				lib.showAwesomeToast(ct, "hello");
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Display the toast!
		
	}
}
