package mejje;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Splash extends Canvas {
	private Image _logo;
	private float _progress;
	private int _width;
	private int _height;

	public void setProgress(float progress) {
		_progress = progress;
		repaint();
	}

	public Splash() {
		setFullScreenMode(true);
		try {
			_logo = Image.createImage("/logo.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		_progress = 0;
		_width = getWidth();
		_height = getHeight();
	}

	protected void paint(Graphics g) {
		if (_progress == 0) {
			g.drawImage(_logo, 0, 0, Graphics.TOP | Graphics.LEFT);
		}
		g.setColor(0x00ffffff);
		g.fillRect(0, _height - 5, (int) (_width * _progress), 5);
	}
}
