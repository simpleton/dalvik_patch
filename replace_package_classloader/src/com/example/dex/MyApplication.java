/**
 * 
 */
package com.example.dex;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import dalvik.system.DexClassLoader;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author simsun
 *
 */
public class MyApplication extends Application {
	private static final String TAG = "MyApplication";
	private static final String SECONDARY_DEX_NAME = "secondary_dex.jar";
	private static final int BUF_SIZE = 8 * 1024;
	private File dexInternalStoragePath;
	public ClassLoader class_loader;
	static {
		Log.e(TAG, "load MyApplication class");
	}
	@Override
	public void onCreate() {
		load_external_dex(SECONDARY_DEX_NAME);
		super.onCreate();
	}

	public void load_external_dex(String file_name) {
        // Before the secondary dex file can be processed by the DexClassLoader,
        // it has to be first copied from asset resource to a storage location.
        dexInternalStoragePath = new File(getDir("dex", Context.MODE_PRIVATE),
                SECONDARY_DEX_NAME);
        if (!dexInternalStoragePath.exists()) {           
            // Perform the file copying in an AsyncTask.
            (new PrepareDexTask()).execute(dexInternalStoragePath);
        } else {
            Log.d(TAG, "[onCreate]dexInternalStoragePath:" + dexInternalStoragePath.getAbsolutePath());
            changeSystemClassLoader(this);
        }
	}
	//FIXME: load duplicated
	private void changeSystemClassLoader(Application app) {
		try {
			Context mBase = new Smith<Context>(app, "mBase").get();
			Object package_info = new Smith<Object>(mBase, "mPackageInfo").get();
			Smith<ClassLoader> package_class_loader = new Smith<ClassLoader>(package_info, "mClassLoader");
			ClassLoader with_second_dex_dcl = buildDexClassLoader(dexInternalStoragePath, package_class_loader.get());
			package_class_loader.set(with_second_dex_dcl);
			class_loader = with_second_dex_dcl;
			try {
				Class<?> clazz = with_second_dex_dcl.loadClass("com.example.dex.lib.LibraryProvider");
				if (clazz != null) 
					Log.d(TAG, "this is " + clazz.getName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private DexClassLoader buildDexClassLoader(File dex_file, ClassLoader parent) {
		File optimized_dex_path = getDir("optDex", Context.MODE_PRIVATE);
		optimized_dex_path.mkdirs();
		DexClassLoader dcl = new DexClassLoader(dex_file.getAbsolutePath(), optimized_dex_path.getAbsolutePath(), null, parent);
		return dcl;
	}
	private class PrepareDexTask extends AsyncTask<File, Void, File> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(File dexInternalStoragePath) {
            super.onPostExecute(dexInternalStoragePath);
            Log.d(TAG, "[AsyncTask]dexInternalStoragePath:" + dexInternalStoragePath.getAbsolutePath());
            changeSystemClassLoader(MyApplication.this);
        }

        @Override
        protected File doInBackground(File... dexInternalStoragePaths) {
            prepareDex(dexInternalStoragePaths[0]);
            return dexInternalStoragePaths[0];
        }
    }
	 // File I/O code to copy the secondary dex file from asset resource to internal storage.
    private boolean prepareDex(File dexInternalStoragePath) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        try {
            bis = new BufferedInputStream(getAssets().open(SECONDARY_DEX_NAME));
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }
}
