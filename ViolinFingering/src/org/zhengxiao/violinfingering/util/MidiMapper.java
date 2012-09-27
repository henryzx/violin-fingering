package org.zhengxiao.violinfingering.util;

import org.zhengxiao.violinfingering.Configs;

import com.leff.midi.MidiFile;

/**
 * NOTE: 指法提高一个八度
 * @author Zheng
 *
 */
public class MidiMapper {

	// Octave|| Note Numbers
	// # ||
	//   ||  C | C# |  D | D# |  E |  F | F# |  G | G# |  A | A# |  B
	// -----------------------------------------------------------------------------
	// 0 || 0  | 1  | 2  |  3 |  4 | 5  |  6 |  7 |  8 |  9 | 10 | 11
	// 1 || 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23
	// 2 || 24 | 25 | 26 | 27 | 28 | 29 | 30 | 31 | 32 | 33 | 34 | 35
	// 3 || 36 | 37 | 38 | 39 | 40 | 41 | 42 |[43 |\44 | 45 | 46 | 47
	// 4 || 48 | 49 | 50 |\ 51| 52 | 53 | 54 | 55 | 56 | 57 |\ 58 | 59
	// 5 || 60 | 61 | 62 | 63 | 64 |\ 65 | 66 | 67 | 68 | 69 | 70 | 71]
	// 6 || 72 | 73 | 74 | 75 | 76 | 77 | 78 | 79 | 80 | 81 | 82 | 83
	// 7 || 84 | 85 | 86 | 87 | 88 | 89 | 90 | 91 | 92 | 93 | 94 | 95
	// 8 || 96 | 97 | 98 | 99 | 100 | 101 | 102 | 103 | 104 | 105 | 106 | 107
	// 9 || 108 | 109 | 110 | 111 | 112 | 113 | 114 | 115 | 116 | 117 | 118 |
	// 119
	// 10 || 120 | 121 | 122 | 123 | 124 | 125 | 126 | 127 |
	//
	// Violin: G3=43 D4=50 A4=57 E5=64

	public final static String[] NOTENAME = { "C", "C#", "D", "D#", "E", "F",
			"F#", "G", "G#", "A", "A#", "B" };
	public final static int G3 = 43+12;
	public final static int D4 = 50+12;
	public final static int A4 = 57+12;
	public final static int E5 = 64+12;

//	public final static int[] NOTEVIEWID = { R.id.A0 };

	public static String Value2Name(int midiNoteValue) {
		int note = midiNoteValue % 12;
		int octave = midiNoteValue / 12;
		return NOTENAME[note] + String.valueOf(octave);
	}

	public static int Value2LineFinger(int midiNoteValue) {
		int violin = midiNoteValue - G3;
		int line = violin / 7;
		int finger = violin % 7;

		if (line > 3) {
			line = line % 4;
		}

		return line * 10 + finger;
	}

	public static int Value2Range(int midiNoteValue) {
		while(midiNoteValue < G3){
			midiNoteValue += 12;
		}
		while (midiNoteValue > E5 + 7) {
			midiNoteValue -= 12;
		}
		return midiNoteValue;
	}
	
	public static boolean isInViolinRange(int noteValue){
		return noteValue >G3 && noteValue < E5+7;
	}
	
	public static boolean isAtViolinLine(int noteValue){
		return noteValue == G3 || noteValue == D4 || noteValue==A4 || noteValue == E5;
	}
	
	public static int atViolinLine(int noteValue){
		if(noteValue<G3){
			return 0;
		}else if(noteValue<=D4){
			return 1;
		}else if(noteValue <= A4){
			return 2;
		}else if(noteValue <= E5){
			return 3;
		}else if (noteValue <= E5+7){
			return 4;
		}else{
			return 5;
		}
	}

	public static boolean isInSameTime(MidiFile mf, long tick1, long tick2) {
		return isInSameTime(getMinQuantifyUnitInTicks(mf), tick1, tick2);
	}

	public static boolean isInSameTime(int minQuantifyUnitInTicks, long tick1,
			long tick2) {

		return Math.abs(tick2 - tick1) < minQuantifyUnitInTicks;
	}

	public static int getMinQuantifyUnitInTicks(MidiFile mf) {
		int minQuantifyUnitInTicks = -1;
		int ticksPerQuarter = mf.getResolution();
		int ticksPer32th = ticksPerQuarter *4 / 32;
		minQuantifyUnitInTicks = Configs.MIN_QUANTIFY_UNIT_IN_32TH
				* ticksPer32th;
		return minQuantifyUnitInTicks;
	}
}
