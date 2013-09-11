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

package com.example.dex;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.dex.lib.LibraryProvider;

public class MainActivity extends Activity {
    private static final String SECONDARY_DEX_NAME = "secondary_dex.jar";
    private static final String TAG = "DexInjectActivity";
    // Buffer size for file copying.  While 8kb is used in this sample, you
    // may want to tweak it based on actual size of the secondary dex file involved.
    private static final int BUF_SIZE = 8 * 1024;
    static {
    	Log.e("TAG", "MainActivity");
    }
    private Button mToastButton = null;
    private ProgressDialog mProgressDialog = null;
    private File dexInternalStoragePath;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mToastButton = (Button) findViewById(R.id.toast_button);
        
        // Before the secondary dex file can be processed by the DexClassLoader,
        // it has to be first copied from asset resource to a storage location.
        dexInternalStoragePath = new File(getDir("dex", Context.MODE_PRIVATE),
                SECONDARY_DEX_NAME);
        if (!dexInternalStoragePath.exists()) {
            mProgressDialog = ProgressDialog.show(this,
                    getResources().getString(R.string.diag_title), 
                    getResources().getString(R.string.diag_message), true, false);
            // Perform the file copying in an AsyncTask.
            (new PrepareDexTask()).execute(dexInternalStoragePath);
        } else {
            mToastButton.setEnabled(true);
            Log.d(TAG, "[onCreate]dexInternalStoragePath:" + dexInternalStoragePath.getAbsolutePath());
          
        }
        DexInjector.inject(getApplication(), dexInternalStoragePath.getAbsolutePath());

        mToastButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                try {

                	create_second_dex_obj(view);
                } catch (Exception exception) {
                    // Handle exception gracefully here.
                    exception.printStackTrace();
                }
            }


        });
    }
	private void create_second_dex_obj(View view) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
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
				lib.showAwesomeToast(view.getContext(), "hello");
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    @Override
    protected void onStart() {
      
    	super.onStart();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
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
    
    private class PrepareDexTask extends AsyncTask<File, Void, File> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mProgressDialog != null) mProgressDialog.cancel();
        }

        @Override
        protected void onPostExecute(File dexInternalStoragePath) {
            super.onPostExecute(dexInternalStoragePath);
            if (mProgressDialog != null) mProgressDialog.cancel();
            Log.d(TAG, "[AsyncTask]dexInternalStoragePath:" + dexInternalStoragePath.getAbsolutePath());
        }

        @Override
        protected File doInBackground(File... dexInternalStoragePaths) {
            prepareDex(dexInternalStoragePaths[0]);
            return dexInternalStoragePaths[0];
        }
    }
}
