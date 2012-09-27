package org.zhengxiao.violinfingering;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Zheng
 *
 */
public class FileListActivity extends ListActivity {

	public static final String EXTRA_FILE = "midi_file_option";
	private File currentDir;
	private FileArrayAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		currentDir = new File("/sdcard/");
		currentDir = Environment.getExternalStorageDirectory();
	}
	
	@Override
	protected void onStart() {
		fill(currentDir);
		super.onStart();
	}
	
	private List<Option> getAssetsMidi(){
		List<Option> assets = new ArrayList<Option>();
		String mMidiFileName = "midi/practice.mid";
		assets.add(new Option("practice.mid", "示例文件", mMidiFileName, true));
		return assets;
	}

	private void fill(File f) {
		boolean[] flags = ensureExternalStorage();
		boolean sdAvailable = flags[0];
		if(!sdAvailable)
			Toast.makeText(this, "没有SD卡", Toast.LENGTH_LONG).show();
		File[] dirs = f.listFiles();
		this.setTitle("当前目录: " + f.getName());
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory())
					dir.add(new Option("\\"+ff.getName(), "", ff
							.getAbsolutePath()));
				else if(ff.getName().endsWith(".mid")){
					fls.add(new Option(ff.getName(), "文件，大小: "
							+ ff.length() + " bytes", ff.getAbsolutePath()));
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		// put Sample Files from assets
		dir.addAll(0, getAssetsMidi());
		if (!f.getName().equalsIgnoreCase("sdcard"))
			dir.add(0, new Option("..", "返回上层", f.getParent()));
		adapter = new FileArrayAdapter(FileListActivity.this,
				R.layout.file_view, dir);
		this.setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase("")
				|| o.getData().equalsIgnoreCase("返回上层")) {
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else {
			onFileClick(o);
		}
	}

	private void onFileClick(Option o) {
		if (!o.name.endsWith(".mid")) {
			Toast.makeText(this, "请选择MIDI文件", Toast.LENGTH_LONG).show();
			Intent main = new Intent(this, MainActivity.class);
			o.isAsset = false;
			main.putExtra(EXTRA_FILE, o);
			startActivity(main);
			return;
		}
		Toast.makeText(this, "File Clicked: " + o, Toast.LENGTH_SHORT).show();
		Intent main = new Intent(this, MainActivity.class);
		main.putExtra(EXTRA_FILE, o);
		startActivity(main);
	}

	static class FileArrayAdapter extends ArrayAdapter<Option> {

		private Context c;
		private int id;
		private List<Option> items;

		public FileArrayAdapter(Context context, int textViewResourceId,
				List<Option> objects) {
			super(context, textViewResourceId, objects);
			c = context;
			id = textViewResourceId;
			items = objects;
		}

		public Option getItem(int i) {
			return items.get(i);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) c
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(id, null);
			}
			final Option o = items.get(position);
			if (o != null) {
				TextView t1 = (TextView) v.findViewById(R.id.TextView01);
				TextView t2 = (TextView) v.findViewById(R.id.TextView02);

				if (t1 != null)
					t1.setText(o.getName());
				if (t2 != null)
					t2.setText(o.getData());

			}
			return v;
		}

	}
	
	
	public static boolean[] ensureExternalStorage(){
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return new boolean[]{mExternalStorageAvailable, mExternalStorageWriteable};
	}
	

	static class Option implements Comparable<Option>, Serializable {
		private static final long serialVersionUID = -674656248362732814L;
		private boolean isAsset;
		private String name;
		private String data;
		private String path;

		public Option(String n, String d, String p) {
			this(n,d,p,false);
		}
		
		public Option(String n, String d, String p, boolean isAsset){
			name = n;
			data = d;
			path = p;
			this.isAsset = isAsset;
		}

		public String getName() {
			return name;
		}

		public String getData() {
			return data;
		}

		public String getPath() {
			return path;
		}
		
		public boolean isAsset() {
			return isAsset;
		}

		public void setAsset(boolean isAsset) {
			this.isAsset = isAsset;
		}
		
		public int compareTo(Option o) {
			if (this.name != null)
				return this.name.toLowerCase().compareTo(
						o.getName().toLowerCase());
			else
				throw new IllegalArgumentException();
		}

		@Override
		public String toString() {
			return "Option:{name:" + name + ", data:" + data + ", path:" + path + "}";
		}

		
		
	}
}
