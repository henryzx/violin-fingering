package org.z.violinfingering.util;

import org.z.violinfingering.R;

import android.app.Activity;
import android.content.Intent;

public class ThemeUtil {
	public final static String INTENT_EXTRA_NIGHTMODE = "nightMode";
	public static boolean isNightMode = false;
	public static int currentTheme = R.style.AppTheme;
	
	public static void changeNightMode(Activity activity, boolean isNightMode){
		ThemeUtil.isNightMode = isNightMode;
		Intent intent = activity.getIntent();
		intent.putExtra(INTENT_EXTRA_NIGHTMODE, isNightMode);
		activity.finish();
		if(isNightMode)
			currentTheme = R.style.AppTheme_Night;
		else
			currentTheme = R.style.AppTheme;
		activity.startActivity(intent);
		
	}
}
