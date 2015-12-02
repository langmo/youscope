/**
 * 
 */
package ch.ethz.csb.youscope.addon.youpong;

import java.io.BufferedInputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;

/**
 * @author langmo
 *
 */
class YouPongSounds
{
	private static final String HIT_SOUND_FILE = "ch/ethz/csb/youscope/addon/youpong/sounds/hit.wav";
	private static final String LOOSE_SOUND_FILE = "ch/ethz/csb/youscope/addon/youpong/sounds/loose.wav";
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
