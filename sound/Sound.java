package sound;

import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Sound {
	private int streak;
	
	public Sound() {
		streak = 0;
	}
	
	public void playComplete() {
		if (streak >= 2) {
			try {
				Path filePath = Paths.get(System.getProperty("user.dir") + "/src/sound/cheering.wav");
				File soundFile = new File(filePath.toString());
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
				streak++;
			} catch (Exception error) {
				System.err.println(error);
			}
		}
		else if (streak == 1) {
			try {
				Path filePath = Paths.get(System.getProperty("user.dir") + "/src/sound/applause_2.wav");
				File soundFile = new File(filePath.toString());
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
				streak++;
			} catch (Exception error) {
				System.err.println(error);
			}
		}
		else {
			try {
				Path filePath = Paths.get(System.getProperty("user.dir") + "/src/sound/applause_1.wav");
				File soundFile = new File(filePath.toString());
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
				streak++;
			} catch (Exception error) {
				System.err.println(error);
			}
		}

	}
}
