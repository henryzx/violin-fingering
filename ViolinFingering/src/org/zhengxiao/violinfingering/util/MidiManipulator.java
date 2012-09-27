package org.zhengxiao.violinfingering.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.util.Log;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

public class MidiManipulator {
	
	public final static int MIN_VELOCITY = 10;

	public static MidiTrack selectViolinTrack(MidiFile mf) {
		MidiTrack T = null; // SelectedTrack
		MidiTrack tempoTrack; // TempoTrack

		List<MidiTrack> tracks = mf.getTracks();
		tempoTrack = mf.getTracks().get(0);
		MidiTrack track1 = mf.getTracks().get(1);

		// 1. 删除TempoTrack和第一个有事件的Track以外的Track
		List<Integer> tracksNumberToRemove = new ArrayList<Integer>(mf.getTrackCount());
		int index = 0;
		for (MidiTrack track : tracks) {
			if (tempoTrack.equals(track)){
			}else if (T == null && track.getEventCount() != 0) {
				T = track;
			}else{
				tracksNumberToRemove.add(index);
			}
			index++;
		}
		Collections.reverse(tracksNumberToRemove);
		for (int trackNumber : tracksNumberToRemove) {
			mf.removeTrack(trackNumber);
		}

		if (track1.equals(T))
			Log.d("test", "Track1 == selectedTrack!");

		// 2. Strip out anything but notes from violin track: T
		// It's a bad idea to modify a set while iterating, so we'll collect
		// the events first, then remove them afterwards
		Iterator<MidiEvent> it = T.getEvents().iterator();
		ArrayList<MidiEvent> eventsToRemove = new ArrayList<MidiEvent>();

		while (it.hasNext()) {
			MidiEvent E = it.next();

			// if (!E.getClass().equals(NoteOn.class)
			// && !E.getClass().equals(NoteOff.class)) {
			if (!(E instanceof NoteOn)) {
				eventsToRemove.add(E);
			}else if(((NoteOn)E).getVelocity()<MIN_VELOCITY){
				eventsToRemove.add(E);
			}
		}

		for (MidiEvent E : eventsToRemove) {
			T.removeEvent(E);
		}

		// filter chord leave only one note

		filterChord(mf, T);

		return T;
	}

	/**
	 * filter chord leave only one note
	 * 
	 * @param mf
	 * @param track
	 */
	public static void filterChord(MidiFile mf, MidiTrack track) {
		List<MidiEvent> eventToRemove = new ArrayList<MidiEvent>();
		List<MidiEvent> group = new LinkedList<MidiEvent>();
		int q = MidiMapper.getMinQuantifyUnitInTicks(mf);
		TreeSet<MidiEvent> events = track.getEvents();
		long nowTick = -1;
		for (MidiEvent event : events) {
			// first time run
			if (nowTick == -1) {
				group.add(event);
				continue;
			}

			// check if current tick is not in the same Time then pick
			// exceptional one and add them to remove list and clear group
			// otherwise add current event to group
			if (MidiMapper.isInSameTime(q, nowTick, event.getTick())) {
				group.add(event);
				nowTick = event.getTick();
			} else {
				group.remove(0);
				eventToRemove.addAll(group);
				group.clear();
			}
		}

		// remove events in eventToRemove list
		for (MidiEvent event : eventToRemove) {
			track.removeEvent(event);
		}
	}

	/**
	 * @param mf
	 * @param track
	 *            注意：假设track只包含NoteOn Event 並且不能包含和絃
	 */
	@SuppressLint("UseSparseArrays")
	public static Map<Long, Integer> generateFingering(MidiFile mf,
			MidiTrack track) {
		Map<Long, Integer> fingerMap = new TreeMap<Long, Integer>();
		Queue<MidiEvent> queue = new LinkedList<MidiEvent>();// 事件队列
		queue.addAll(track.getEvents());
		MidiEvent current = null;
		while ((current = queue.poll()) != null) {

			// 逻辑：1. 如果 cur 在弦上，则需要判断是否用空弦，否则直接11对应、返回
			// 2.如果 nextLine 在 curLine的外弦上，或为最后一个音，则为空弦，否则为4指、返回
			if(!(current instanceof NoteOn)) continue;
			NoteOn cur = (NoteOn) current;
			final long curTick = current.getTick();
			if(curTick == 481){
				Log.d("tick", String.valueOf(curTick));
			}
			int curValue = cur.getNoteValue();

			// 如果 cur 在弦上，则需要判断是否用空弦，否则直接11对应、返回
			if (MidiMapper.isAtViolinLine(curValue)) {
				final MidiEvent next = queue.peek();
				if (next != null) {
					int nextValue = ((NoteOn) next).getNoteValue();
					int curLine = MidiMapper.atViolinLine(curValue);
					int nextLine = MidiMapper.atViolinLine(nextValue);
					if (nextLine > curLine)
						curValue *= -1;
				} else {
					curValue *= -1;
				}
			}
			fingerMap.put(curTick,curValue);

		}

		return fingerMap;
	}

	public static void setTempo(MidiFile mf, float targetBpm) {
		// 验证参数
		if (!(targetBpm > 0) || mf == null)
			return;
		MidiTrack T = mf.getTracks().get(0);

		Iterator<MidiEvent> it = T.getEvents().iterator();
		while (it.hasNext()) {
			MidiEvent E = it.next();

			if (E.getClass().equals(Tempo.class)) {

				Tempo tempo = (Tempo) E;
				// tempo.setBpm(tempo.getBpm() / 2);
				tempo.setBpm(targetBpm);
			}
		}
	}
}
