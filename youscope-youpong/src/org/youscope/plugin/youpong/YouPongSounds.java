/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.youpong;

import java.io.BufferedInputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import org.youscope.clientinterfaces.YouScopeClient;

/**
 * @author langmo
 *
 */
class YouPongSounds
{
	private static final String HIT_SOUND_FILE = "org/youscope/plugin/youpong/sounds/hit.wav";
	private static final String LOOSE_SOUND_FILE = "org/youscope/plugin/youpong/sounds/loose.wav";
	private URL hitSoundURL;
	private URL looseSoundURL;
	private YouScopeClient client;
	public YouPongSounds(YouScopeClient client)  
	{ 
		hitSoundURL = YouPongSounds.class.getClassLoader().getResource(HIT_SOUND_FILE);
		looseSoundURL = YouPongSounds.class.getClassLoader().getResource(LOOSE_SOUND_FILE);
		this.client = client;
	}
	private Clip playAudioFile(URL soundURL) throws Exception
	{
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
		BufferedInputStream bufferedInputStream = new BufferedInputStream(audioInputStream);
	    AudioFormat af = audioInputStream.getFormat();
	    int size = (int) (af.getFrameSize() * audioInputStream.getFrameLength());
	    byte[] audio = new byte[size];
	    DataLine.Info info = new DataLine.Info(Clip.class, af, size);
	    bufferedInputStream.read(audio, 0, size);
	    Clip clip = (Clip) AudioSystem.getLine(info);
	    clip.open(af, audio, 0, size); 
	    clip.start();
	    bufferedInputStream.close();
	    return clip;        

	} 
	public void playHit()
	{
		if(hitSoundURL != null)
		{
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						playAudioFile(hitSoundURL);
					}
					catch(Exception e)
					{
						client.sendError("Could not play sound with URL "+ HIT_SOUND_FILE + ".", e);
						hitSoundURL = null;
					}
				}
			}).start();
		}
	}
	public void playLoose()
	{
		if(looseSoundURL != null)
		{
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						playAudioFile(looseSoundURL);
					}
					catch(Exception e)
					{
						client.sendError("Could not play sound with URL "+ LOOSE_SOUND_FILE + ".", e);
						hitSoundURL = null;
					}
				}
			}).start();
		}
	}
}
