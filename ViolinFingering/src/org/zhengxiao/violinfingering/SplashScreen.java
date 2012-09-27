package org.zhengxiao.violinfingering;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class SplashScreen extends Activity {
	
	static final long TIMEOUT_MILLIS = 3 * 1000;
	boolean isLeaving = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				leave(null);
			}
        }, TIMEOUT_MILLIS);
    }



	public synchronized void leave(View v){
    	if(isLeaving) return;
    	isLeaving = true;
    	Intent list = new Intent(SplashScreen.this,FileListActivity.class);
		SplashScreen.this.startActivity(list);
    	finish();
    }

    
}
