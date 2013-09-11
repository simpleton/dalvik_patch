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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.dex.lib.LibraryProvider;

public class MainActivity extends Activity {
	private static final String SECONDARY_DEX_NAME = "secondary_dex.jar";
    private static final String TAG = "DexInjectActivity";

    static {
    	Log.e("TAG", "load MainActivity class");
    }
    private Button mToastButton = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mToastButton = (Button) findViewById(R.id.toast_button);
        // Before the secondary dex file can be processed by the DexClassLoader,
        // it has to be first copied from asset resource to a storage location.
       

        mToastButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                try {
                	LibraryProvider lib = new LibraryProvider();
            		lib.showAwesomeToast(view.getContext(), "hello");
                	create_second_dex_obj(view);
                } catch (Exception exception) {
                    // Handle exception gracefully here.
                    exception.printStackTrace();
                }
            }            
        });
    }
	private void create_second_dex_obj(View view) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		
//				Class<?> clazz = null;
//				try {
//					clazz = Class.forName("com.example.dex.lib.LibraryProvider");
//					//clazz = ((MyApplication)getApplication()).class_loader.loadClass("com.example.dex.lib.LibraryProvider");
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				LibraryProvider lib;
//				try {
//					if (clazz != null) {
//						lib = (LibraryProvider) clazz.newInstance();
//						lib.showAwesomeToast(view.getContext(), "hello");
//					}
//				} catch (IllegalAccessException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InstantiationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
    	TestClass.test(view.getContext());		
	}
	
    @Override
    protected void onStart() {
      
    	super.onStart();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    }  
}
