package org.zhengxiao.violinfingering;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.zhengxiao.violinfingering.R;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;


/**
 * NOTE: tag提高了1个八度
 * @author Zheng
 *
 */
public class FingerPadController {
	private Activity mActivity;
	Map<Integer, View> mFingerViews;
	List<View> mVisibleView = new LinkedList<View>();

	public FingerPadController(Activity activity) {
		super();
		this.mActivity = activity;
		Map<Integer, View> fingerViews = new HashMap<Integer, View>();

		ViewGroup vg = (ViewGroup) activity.findViewById(R.id.fingerpad);

		int count = vg.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = vg.getChildAt(i);
			Object tag = v.getTag();
			try {
				if (tag == null)
					continue;
				Integer tagNumber = Integer.valueOf(String.valueOf(tag));
				if(tagNumber >= 0)
					tagNumber += 12;
				else{
					tagNumber -= 12;
				}
				v.setVisibility(View.INVISIBLE);
				fingerViews.put(tagNumber, v);
			} catch (NumberFormatException e) {
				continue;
			}

		}
		mFingerViews = fingerViews;
	}

	/**
	 * @param tag
	 * @param visibility
	 * @return true: 变化, false:为空或没有变化
	 */
	public boolean displayView(int tag, int visibility) {
		View v = mFingerViews.get(tag);
		if (v == null)
			return false;
		if (v.getVisibility() == visibility)
			return false;
		v.setVisibility(visibility);
		clearView();
		mVisibleView.add(v);
		return true;
	}
	
	public void clearView(){
		for(View v: mVisibleView){
			v.setVisibility(View.INVISIBLE);
		}
		mVisibleView.clear();
	}

}
