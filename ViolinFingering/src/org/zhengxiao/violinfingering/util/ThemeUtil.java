package org.zhengxiao.violinfingering.util;

import org.zhengxiao.violinfingering.R;

import android.app.Activity;
import android.content.Intent;

public class ThemeUtil {
	public final static String INTENT_EXTRA_NIGHTMODE = "nightMode";
	
	public static int currentTheme = R.style.AppTheme;
	
	public static void changeNightMode(Activity activity, boolean isNightMode){
		Intent intent = activity.getIntent();
		intent.putExtra(INTENT_EXTRA_NIGHTMODE, isNightMode);
		activity.finish();
		if(isNightMode)
//			activity.setTheme(R.style.AppTheme_Night);
			currentTheme = R.style.AppTheme_Night;
		else
//			activity.setTheme(R.style.AppTheme);
			currentTheme = R.style.AppTheme;
		activity.startActivity(intent);
		
	}
}
