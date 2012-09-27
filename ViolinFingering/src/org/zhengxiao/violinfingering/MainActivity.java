package org.zhengxiao.violinfingering;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Map;

import org.zhengxiao.violinfingering.util.FileManager;
import org.zhengxiao.violinfingering.util.MidiManipulator;
import org.zhengxiao.violinfingering.util.MidiMapper;
import org.zhengxiao.violinfingering.util.ThemeUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.leff.midi.MidiFile;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.util.MidiEventListener;
import com.leff.midi.util.MidiProcessor;

/**
 * @author Zheng
 * 
 */
public class MainActivity extends Activity implements MidiEventListener,
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

	private static final String CACHEPATH_PLAYBACK_MID = "/playback.mid";
	MidiProcessor mProcessor;
	String mMidiFileName = "midi/practice.mid";// "midi/flourish.mid";
	boolean isAsset = true;
	Map<Long, Integer> mFingerMap;
	Handler mHandler = new MidiHandler(this);

	public static class MidiHandler extends Handler {
		WeakReference<MainActivity> activity;

		public MidiHandler(MainActivity mainActivity) {

			this.activity = new WeakReference<MainActivity>(mainActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			final int value = msg.arg1;
			final int velocity = msg.arg2;
			final long tick = (Long) msg.obj;
//			activity.get().mTextView.setText("tick=" + tick + "value=" + value
//					+ ",note=" + MidiMapper.Value2Name(value) + ",velocity="
//					+ velocity);
			activity.get().mTextView.setText(MidiMapper.Value2Name(value));
			activity.get().mFingerPadController.displayView(
					activity.get().mFingerMap.get(tick), View.VISIBLE);
			super.handleMessage(msg);
		}
	};

	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	FingerPadController mFingerPadController;

	Boolean busy = false;
	TextView mTextView;
	Button mButtonPlay;
	Button mButtonStop;
	ProgressDialog mProgressDialog;
	MediaPlayer mMediaPlayer = new MediaPlayer();
	SoundPool mSoundPool;
	int sound_id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FileListActivity.Option option = (FileListActivity.Option) getIntent()
				.getExtras().get(FileListActivity.EXTRA_FILE);
		
		isAsset = option.isAsset();
		mMidiFileName = option.getPath();
		setLocale2("nt");
		setContentView(R.layout.activity_main);

		mTextView = (TextView) findViewById(R.id.textView_log);
		mButtonPlay = (Button) findViewById(R.id.button_play);
		// mButtonPlay.setClickable(false);
		mButtonPlay.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				mButtonPlay.setClickable(false);
				mProgressDialog = ProgressDialog.show(MainActivity.this,
						getString(R.string.loading_title),
						getString(R.string.loading_message));
				Log.d("user interface", "mButtonPlay");
				Thread prepareMidiAndMediaPlayer = new Thread(new Runnable() {
					public void run() {
						try {
							loadMidi(mMidiFileName);
							// initSoundPool();
							initMediaPlayer();
						} catch (Exception e) {
							e.printStackTrace();
							onError(mMediaPlayer,0,0);
						}
					}
				});
				prepareMidiAndMediaPlayer.start();
				// wait for onPrepare();
			}
		});

		mButtonStop = (Button) findViewById(R.id.button_stop);
		mButtonStop.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.d("user interface", "mButtonStop");
				if (mProcessor != null) {
					mProcessor.stop();
				}
			}
		});
		boolean isChecked = false;
		Object night = getIntent().getExtras().get(ThemeUtil.INTENT_EXTRA_NIGHTMODE);
		if(night !=null){
			isChecked = (Boolean)night;
		}
		ToggleButton bu = (ToggleButton) findViewById(R.id.toggleButton_night);
		bu.setChecked(isChecked);
		bu.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ThemeUtil.changeNightMode(MainActivity.this, isChecked);
			}
		});
		
		mFingerPadController = new FingerPadController(this);

	}

	private void loadMidi(String midiFileName) throws Exception {
		Log.d("init", "loadMidi");
		MidiFile midi = null;
			if (isAsset) {
				InputStream is = getAssets().open(midiFileName);
				midi = new MidiFile(is);
			} else {
				midi = new MidiFile(new File(mMidiFileName));
			}

		mProcessor = new MidiProcessor(midi);
		// EventPrinter ep = new EventPrinter("Individual Listener");
		mProcessor.registerEventListener(this, Tempo.class);
		mProcessor.registerEventListener(this, NoteOn.class);
		mProcessor.registerEventListener(this, NoteOff.class);

		mFingerMap = MidiManipulator.generateFingering(midi,
				MidiManipulator.selectViolinTrack(midi));
		File cache = new File(getCacheDir() + CACHEPATH_PLAYBACK_MID);
		if (cache.exists())
			cache.delete();
		cache.createNewFile();
//		midi.writeToFile(cache);
		if(isAsset)
			midi.writeToFile(cache);
		else
			FileManager.copyFile(new File(mMidiFileName), cache);
//		File nf = new File(Environment.getExternalStorageDirectory()+"/playback.mid");
//		if(nf.exists())
//			nf.delete();
//		nf.createNewFile();
//		FileManager.copyFile(new File(mMidiFileName), nf);
	}

	public void initSoundPool() throws Exception {
		Log.d("init", "initSoundPool");
		// InputStream fis = new FileInputStream(getCacheDir() +
		// CACHEPATH_PLAYBACK_MID);
		File f = new File(getCacheDir() + CACHEPATH_PLAYBACK_MID);
		FileInputStream is = new FileInputStream(f);
		int length = is.available();
		FileDescriptor fd = is.getFD();
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		sound_id = mSoundPool.load(fd, 0, length, 1);
		is.close();
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
		Log.d("processor", "mediaplayer onPrepared");
		if (mProcessor != null)
			mProcessor.start();
		mSoundPool.play(sound_id, 1f, 1f, 1, 0, 1);
	}

	public void initMediaPlayer() throws Exception {
		Log.d("init", "initMediaPlayer");
		mMediaPlayer.reset();
		FileInputStream fis = new FileInputStream(getCacheDir() + CACHEPATH_PLAYBACK_MID);
		if (!(fis.available() > 0)) {
			fis.close();
			Log.e("mediaPlayer", "INIT FAILED: file size = 0");
			return;
		}
		FileDescriptor fd = fis.getFD();
		mMediaPlayer.setDataSource(fd);
//		mMediaPlayer.setOnPreparedListener(this);
//		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.prepare();
		onPrepared(mMediaPlayer);
		fis.close();

	}

	@Override
	protected void onStop() {
		onStop(false);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mMediaPlayer.release();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onStart(boolean fromBeginning) {
		Log.d("processor", "onStart,beginning?" + fromBeginning);
		if (mMediaPlayer != null) {
			mMediaPlayer.start();
		}
	}

	public void onEvent(MidiEvent event, long ms) {
		Log.d("processor", "onEvent");
		synchronized (busy) {
			if (busy == true)
				return;
			busy = true;
		}
		if (event instanceof NoteOn) {
			final NoteOn note = (NoteOn) event;
			// if (note.getChannel() != PLAY_CHANNEL || note.getVelocity() < 10)
			// return;
			final int value = note.getNoteValue();
			final int velocity = note.getVelocity();
			final long tick = note.getTick();
			Message message = mHandler.obtainMessage();
			message.obj = tick;
			message.arg1 = value;
			message.arg2 = velocity;
			message.sendToTarget();

		} else if (event instanceof NoteOff) {
			// final NoteOff note = (NoteOff) event;
			// int value = note.getNoteValue();
			// mFingerPadController.displayView(MidiMapper.Value2Range(value),
			// View.INVISIBLE);
			// Log.d("NoteOff", "");
		} else if (event instanceof Tempo) {
			Tempo tempo = (Tempo) event;
			Log.d("tempo", String.valueOf(tempo.getBpm()));

		}

		synchronized (busy) {
			busy = false;
		}
	}

	public void onStop(boolean finished) {
		mButtonPlay.setClickable(true);
		Log.d("processor", "onStop, finished?" + finished);
		if(mMediaPlayer.isPlaying())
			mMediaPlayer.stop();
		mMediaPlayer.reset();
		mHandler.post(new Runnable() {
			public void run() {
				mFingerPadController.clearView();
			}
		});
	}

	public void setLocale(String language_code) {
		Resources res = this.getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(language_code.toLowerCase());
		res.updateConfiguration(conf, dm);
	}

	public void setLocale2(String languageToLoad) {
		// String languageToLoad = "fa"; // your language
		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
	}

	public void onPrepared(MediaPlayer player) {
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
		Log.d("processor", "mediaplayer onPrepared");
		if (mProcessor != null)
			mProcessor.start();
	}

	@Override
	public boolean onError(MediaPlayer player, int arg1, int arg2) {
		Log.i("error","MediaPlayer throw an Error");
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
		mButtonPlay.setClickable(true);
		try {
			mMediaPlayer.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


}
