package com.george.getreadychat;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

public class ChatSound {

    public static final int S1 = R.raw.sound;

    private static SoundPool soundPool;
    private static HashMap soundPoolMap;

    /** Populate the SoundPool*/
    public static void initSounds(Context context) {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new HashMap(1);
        soundPoolMap.put( S1, soundPool.load(context, R.raw.sound, 1) );

    }

    /** Play a given sound in the soundPool */
    public static void playSound(Context context, int soundID) {
        if(soundPool == null || soundPoolMap == null){
            initSounds(context);
        }
        float volume = 1;// whatever in the range = 0.0 to 1.0
        // play sound with same right and left volume, with a priority of 1,
        // zero repeats (i.e play once), and a playback rate of 1f
        soundPool.play(S1, volume, volume, 1, 0, 1f);
    }

}
