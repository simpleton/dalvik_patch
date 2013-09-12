package com.example.dex;

import android.content.Context;
import android.util.Log;

import com.example.dex.lib.LibraryProvider;
import com.example.dex.lib2.OtherLibraryProvider;

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
			e.printStackTrace();
		}
		LibraryProvider lib;
		try {
			if (clazz != null) {
				lib = (LibraryProvider) clazz.newInstance();
				lib.showAwesomeToast(ct, "hello");
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		// Display the toast!
	}
	
	public static void test_other(Context ct) {
		try {
			Class.forName("com.example.dex.lib2.OtherLibraryProvider");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//LibraryProvider lib = new LibraryProvider();
		Class<?> clazz = null;
		try {
			clazz = Class.forName("com.example.dex.lib2.OtherLibraryProvider");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		OtherLibraryProvider lib;
		try {
			if (clazz != null) {
				lib = (OtherLibraryProvider) clazz.newInstance();
				lib.showAwesomeToast(ct, "i'm from other dex");
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		// Display the toast!
		
	}
}
