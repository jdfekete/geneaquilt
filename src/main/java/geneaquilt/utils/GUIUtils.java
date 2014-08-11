/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
/*
 * Copyright (c) 2008 Pierre Dragicevic <dragice@lri.fr>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package geneaquilt.utils;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.color.ColorSpace;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.MemoryImageSource;
import java.awt.image.RGBImageFilter;
import java.awt.image.ShortLookupTable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Various useful GUI utilities that are missing in Swing (or so I think).
 * 
 * @author Pierre Dragicevic
 */

@SuppressWarnings("all")
public class GUIUtils {

	/**
	 * This is a temporary fix for the videos not showing in fullscreen timeline mode.
	 */
	public static final boolean USE_TRUE_FULL_SCREEN = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1;
	
	public final static Cursor NO_CURSOR = Toolkit.getDefaultToolkit()
	.createCustomCursor(
			Toolkit.getDefaultToolkit().createImage(
					new MemoryImageSource(16, 16, new int[16 * 16], 0,
							16)), new Point(0, 0), "invisibleCursor");

	/**
	 * Centers a window on the primary display
	 */
	public static void centerOnPrimaryScreen(Window toplevel) {
		Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
		toplevel.setLocation((screenRes.width - toplevel.getWidth()) / 2,
				(screenRes.height - toplevel.getHeight()) / 2);
	}

	/**
	 * Centers a window on the primary display
	 */
	public static void centerOnPrimaryScreen(Window toplevel, int newwidth,
			int newheight) {
		Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
		toplevel.setBounds((screenRes.width - newwidth) / 2,
				(screenRes.height - newheight) / 2, newwidth, newheight);
	}

	/**
	 * Makes a window full screen (method 1). Returns false if not supported.
	 */
	public static boolean setFullScreen(Window toplevel, boolean fullscreen) {
		GraphicsDevice device;
		device = GraphicsEnvironment.getLocalGraphicsEnvironment()
		.getDefaultScreenDevice();
		if (USE_TRUE_FULL_SCREEN && device.isFullScreenSupported()) {
			boolean currentlyfullscreen = device.getFullScreenWindow() == toplevel;
			if (fullscreen != currentlyfullscreen)
				device.setFullScreenWindow(toplevel);
			return true;
		}
		return false;
	}

	/**
	 * Makes a window full screen (method 2)
	 */
	public static void fillPrimaryScreen(Window toplevel) {
		Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
		toplevel.setBounds(0, 0, screenRes.width, screenRes.height);
	}

	/**
	 * Manages a waiting cursor. Make sure you always pass the same window
	 * argument.
	 */
	static int computationCount = 0;
	
	static class WaitMessageWindow extends JWindow {
		final String defaultMessage = "Please wait...";
		final JLabel lbl;
		public WaitMessageWindow() {
			super();
			lbl = new JLabel(defaultMessage);
			lbl.setOpaque(true);
			lbl.setBackground(Color.white);
			lbl.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.black),
				BorderFactory.createEmptyBorder(5, 10, 5, 10)
			));
			getContentPane().add(lbl, BorderLayout.CENTER);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			pack();
			setVisible(false);
			setAlwaysOnTop(true);
		}
		public void setVisible(boolean visible) {
			if (!isVisible() && visible) {
				super.setVisible(true);
				GUIUtils.centerOnPrimaryScreen(this);
			} else if (isVisible() && !visible) {
				super.setVisible(false);	
			}			
		}
		public void setMessage(String msg) {
			lbl.setText(msg);
			pack();
		}
	};
	static WaitMessageWindow computationMessageWindow = new WaitMessageWindow();
	

	public static void beginLongComputation(Window window, String message) {
		computationCount++;
		computationMessageWindow.setMessage(message);
		if (computationCount == 1) {
			window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			computationMessageWindow.setVisible(true);
		}
	}
	
	public static void updateComputationMessage(String message) {
		computationMessageWindow.setMessage(message);
	}

	public static void endLongComputation(Window window) {
		computationCount--;
		if (computationCount == 0) {
			window.setCursor(Cursor.getDefaultCursor());
			window.toFront();
			computationMessageWindow.setVisible(false);
		}
	}
	
	public static void grow(Rectangle2D r, float amountx, float amounty) {
		r.setRect(r.getX() - amountx, r.getY() - amounty, r.getWidth() + amountx*2, r.getHeight() + amounty*2);
	}

	/**
	 * Mixes two colors.
	 */
	public static Color mix(Color c0, Color c1, float amount) {
		float r0 = c0.getRed() / 255f;
		float g0 = c0.getGreen() / 255f;
		float b0 = c0.getBlue() / 255f;
		float a0 = c0.getAlpha() / 255f;
		float r1 = c1.getRed() / 255f;
		float g1 = c1.getGreen() / 255f;
		float b1 = c1.getBlue() / 255f;
		float a1 = c1.getAlpha() / 255f;
		return new Color(bound01(r0 + (r1 - r0) * amount), bound01(g0
				+ (g1 - g0) * amount), bound01(b0 + (b1 - b0) * amount),
				bound01(a0 + (a1 - a0) * amount));
	}

	private static float bound01(float x) {
		if (x < 0)
			x = 0;
		if (x > 1)
			x = 1;
		return x;
	}

	/**
	 * Changes the opacity of a color
	 */
	public static Color multiplyAlpha(Color c0, float alpha) {
		if (alpha == 1)
			return c0;
		float r0 = c0.getRed() / 255f;
		float g0 = c0.getGreen() / 255f;
		float b0 = c0.getBlue() / 255f;
		float a0 = c0.getAlpha() / 255f;
		return new Color(r0, g0, b0, bound01(a0 * alpha));
	}

	/**
	 * Converts an image to grayscale
	 */
	public static Image toGrayscale(Image source) {
		if (source == null)
			return null;
		BufferedImage src = new BufferedImage(source.getWidth(null), source
				.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		src.getGraphics().drawImage(source, 0, 0, null);
		BufferedImageOp op = new ColorConvertOp(ColorSpace
				.getInstance(ColorSpace.CS_GRAY), null);
		return op.filter(src, null);
	}

	/**
	 * For testing lookup tables
	 */
	public static Image toInverseVideo(Image source) {
		//
		BufferedImage src = new BufferedImage(source.getWidth(null), source
				.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = src.getGraphics();
		g.drawImage(source, 0, 0, null);
		//
		short[][] lookup = new short[4][256];
		for (int c = 0; c < 4; c++) {
			for (short b = 0; b < 256; b++) {
				if (c == 3)
					lookup[c][b] = b;
				else
					lookup[c][b] = (short)(255 - b);
			}
		}
		LookupTable table = new ShortLookupTable(0, lookup);
		LookupOp op = new LookupOp (table, null);
		return op.filter(src, null);
	}

	/**
	 * Creates a monochrome image
	 */
	public static Image toMonochrome(Image source, Color color, float opacity) {
		// If we don't do this, createImage will not compute the image right away
		BufferedImage src = new BufferedImage(source.getWidth(null), source
				.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = src.getGraphics();
		g.drawImage(source, 0, 0, null);
		//
		final int r0 = color.getRed();
		final int g0 = color.getGreen();
		final int b0 = color.getBlue();
		final int a0 = (int)(opacity * 255);
		//
		RGBImageFilter filter = new RGBImageFilter() {
			{
				// The filter's operation does not depend on the
				// pixel's location, so IndexColorModels can be
				// filtered directly.
				canFilterIndexColorModel = true;
			}

			public int filterRGB(int x, int y, int rgba) {
				int r = (rgba >> 16) & 0xff ;
				int g = (rgba >> 8) & 0xff ;
				int b = rgba & 0xff ;
				//int a = (rgba >> 24) & 0xff ;

				float br = (r*0.8f + g*0.65f + b)/625f;
				if (br < 0.5f) {
					r = (int)(r0 * br * 2);
					g = (int)(g0 * br * 2);
					b = (int)(b0 * br * 2);
				} else {
					r = Math.min(255, (int)(r0 + (br - 0.5f) * 255 * 2f));
					g = Math.min(255, (int)(g0 + (br - 0.5f) * 255 * 2f));
					b = Math.min(255, (int)(b0 + (br - 0.5f) * 255 * 2f));
				}

				return((a0 << 24) | (r << 16) | (g << 8) | b);
			}
		};
		//
		ImageProducer producer = new FilteredImageSource(src.getSource(), filter);
		Image res = Toolkit.getDefaultToolkit().createImage(producer);
		return res;
	}

	/**
	 * Clears an image (makes it totally transparent)
	 */
	public static Image createTransparentImage(int width, int height) {
		BufferedImage im = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = im.createGraphics();
		g2D
		.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR,
				0.0f));
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, width, height);
		g2D.fill(rect);
		return im;
	}

	/**
	 * This is a simpler version of toMonochrome
	 */
	public static Image colorizeImage(Image source, Color c, float amount) {
		if (source == null)
			return null;
		BufferedImage src = new BufferedImage(source.getWidth(null), source
				.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = src.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.setColor(multiplyAlpha(c, amount));
		g.fillRect(0, 0, src.getWidth(null), src.getHeight(null));
		return src;
	}

	/**
	 * 
	 */
	public static Image roundImage(Image source, int radius, float alpha) {
		if (source == null)
			return null;
		int width = source.getWidth(null);
		int height = source.getHeight(null);
		BufferedImage src = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) src.getGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, width, height);
		g.fill(rect);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				alpha));
		g.clip(new RoundRectangle2D.Float(0, 0, width - 1, height - 1, radius,
				radius));
		g.drawImage(source, 0, 0, null);
		return src;
	}

	public static void addGlobalKeyListener(final int keycode,
			final ActionListener a, final String command) {
		addGlobalKeyListener(keycode, 0, a, command);
	}

	public static void addGlobalKeyListener(final int keycode, final int modifiers,
			final ActionListener a, final String command) {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent e) {
				if (e instanceof KeyEvent && e.getID() == KeyEvent.KEY_PRESSED) {
					if (((KeyEvent) e).getKeyCode() == keycode && ((KeyEvent) e).getModifiers() == modifiers)
						a.actionPerformed(new ActionEvent(new GUIUtils(),
								KeyEvent.KEY_PRESSED, command));
				}
			}
		}, AWTEvent.KEY_EVENT_MASK);
	}

	// The following adds a separate & fast auto-repeat feature to the
	// keyboard event handling mechanism, and removes the default
	// auto-repeat.

	private static Vector<AdvancedKeyListenerFilter> advancedListeners = new Vector<AdvancedKeyListenerFilter>();

	public static interface AdvancedKeyListener extends KeyListener {
		public void keyPressedOnce(KeyEvent e);
		public void keyRepeated(KeyEvent e);
	}

	private static class AdvancedKeyListenerFilter implements KeyListener {
		AdvancedKeyListener delegate;

		boolean autorepeat;

		final Hashtable<Integer, KeyEvent> keysDown = new java.util.Hashtable<Integer, KeyEvent>();

		private Timer keyRepeatTimer = new Timer(10, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repeatKeys(); //cobertura:ignore
			} //cobertura:ignore
		});

		AdvancedKeyListenerFilter(AdvancedKeyListener destination,
				boolean autorepeat) {
			this.autorepeat = autorepeat;
			this.delegate = destination;
		}

		boolean repeatKeys() {
			for (Enumeration<Integer> e = keysDown.keys(); e.hasMoreElements();) {
				delegate.keyRepeated(keysDown.get(e.nextElement()));
			}
			return keysDown.size() > 0;
		}

		public void keyPressed(KeyEvent e) {
			// 
			delegate.keyPressed(e);
			if (keysDown.containsKey(e.getKeyCode()))
				return;
			keysDown.put(e.getKeyCode(), e);

			// 
			delegate.keyPressedOnce(e);

			// 
			if (keysDown.size() == 1 && autorepeat) {
				keyRepeatTimer.start();
			}
		}

		public void keyReleased(KeyEvent e) {
			//
			if (!keysDown.containsKey(e.getKeyCode()))
				return;
			keysDown.remove(e.getKeyCode());

			//
			if (keysDown.size() == 0 && autorepeat)
				keyRepeatTimer.stop();

			//
			delegate.keyReleased(e);
		}

		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			delegate.keyTyped(arg0);
		}
	}

	/**
	 * Adds an advanced key listener to a component, or adds a global listener if
	 * the component is null.
	 * 
	 * This method will remove the auto-repeat feature, which is most of
	 * the time unwanted. There is a separate option to activate an autorepeat that
	 * is faster than the system one and triggers a separate callback.
	 */
	public static void addAdvancedKeyListener(Component c, final AdvancedKeyListener l, boolean autorepeat) {
		final AdvancedKeyListenerFilter filterListener = new AdvancedKeyListenerFilter(l, autorepeat);
		if (c != null)
			// add a component listener
			c.addKeyListener(filterListener);
		else {
			// add a global listener
			Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
				public void eventDispatched(AWTEvent e) {
					if (e instanceof KeyEvent) {
						switch (e.getID()) {
						case KeyEvent.KEY_PRESSED:
							filterListener.keyPressed((KeyEvent)e);
							break;
						case KeyEvent.KEY_RELEASED:
							filterListener.keyReleased((KeyEvent)e);
							break;
						case KeyEvent.KEY_TYPED:
							filterListener.keyTyped((KeyEvent)e);
							break;
						}
					}
				}
			}, AWTEvent.KEY_EVENT_MASK);
		}
		advancedListeners.add(filterListener);
	}

	public static boolean repeatKeys() {
		int N = advancedListeners.size();
		boolean needsRepeat = false;
		for (int i = 0; i < N; i++)
			needsRepeat |= advancedListeners.elementAt(i).repeatKeys();
		return needsRepeat;
	}

}
