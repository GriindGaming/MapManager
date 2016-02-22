/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.mapmanager;

import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ArrayImage {

	private int[] array;
	private int   width;
	private int   height;

	//Only used if the cache is enabled
	private Object packetData;

	private int imageType = BufferedImage.TYPE_4BYTE_ABGR;

	public ArrayImage(BufferedImage image) {
		this.imageType = image.getType();

		this.width = image.getWidth();
		this.height = image.getHeight();
		int[][] intArray = ImageToArray(image);
		int length = width * height;
		this.array = new int[length];
		for (int x = 0; x < intArray.length; x++) {
			for (int y = 0; y < intArray[x].length; y++) {
				array[y * image.getWidth() + x] = intArray[x][y];
			}
		}

	}

	public ArrayImage(int[][] data) {
		this.array = new int[data.length * data[0].length];
		this.width = data.length;
		this.height = data[0].length;
		for (int x = 0; x < data.length; x++) {
			for (int y = 0; y < data[x].length; y++) {
				array[y * data.length + x] = data[x][y];
			}
		}
	}

	protected Object generatePacketData() {
		if (MapManager.Options.CACHE_DATA && this.packetData != null) { return this.packetData; }

		Object dataObject = null;

		if (Minecraft.getVersion().contains("1_7")) {
			byte[][] dataArray = new byte[128][131];
			for (int x = 0; x < 128; x++) {
				byte[] bytes = new byte[131];

				bytes[1] = (byte) x;
				for (int y = 0; y < 128; y++) {
					bytes[y + 3] = MapSender.matchColor(new Color(getRGB(x, y), true));
				}

				dataArray[x] = bytes;
			}

			dataObject = dataArray;
		} else if (Minecraft.getVersion().contains("1_8")) {
			byte[] data = new byte[128 * 128];
			Arrays.fill(data, (byte) 0);
			for (int x = 0; x < 128; x++) {
				for (int y = 0; y < 128; y++) {
					data[y * 128 + x] = MapSender.matchColor(new Color(getRGB(x, y), true));
				}
			}

			dataObject = data;
		}

		if (MapManager.Options.CACHE_DATA) {
			this.packetData = dataObject;
			return this.packetData;
		} else {
			return dataObject;
		}
	}

	public int getRGB(int x, int y) {
		return array[y * width + x];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public BufferedImage toBuffered() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), this.imageType);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, array[y * getWidth() + x]);
			}
		}
		return image;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		ArrayImage that = (ArrayImage) o;

		if (width != that.width) { return false; }
		if (height != that.height) { return false; }
		return Arrays.equals(array, that.array);

	}

	@Override
	public int hashCode() {
		int result = array != null ? Arrays.hashCode(array) : 0;
		result = 31 * result + width;
		result = 31 * result + height;
		return result;
	}

	protected static int[][] ImageToArray(BufferedImage image) {
		int[][] array = new int[image.getWidth()][image.getHeight()];
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				array[x][y] = image.getRGB(x, y);
			}
		}
		return array;
	}

	protected static boolean ImageContentEqual(BufferedImage b1, BufferedImage b2) {
		if (b1 == null || b2 == null) { return false; }
		// if (b1.equals(b2)) return true;
		if (b1.getWidth() != b2.getWidth()) { return false; }
		if (b1.getHeight() != b2.getHeight()) { return false; }
		for (int y = 0; y < b1.getHeight(); y++) {
			for (int x = 0; x < b1.getWidth(); x++) {
				if (b1.getRGB(x, y) != b2.getRGB(x, y)) { return false; }
			}
		}
		return true;
	}

	protected static boolean ImageContentEqual(ArrayImage b1, ArrayImage b2) {
		if (b1 == null || b2 == null) { return false; }
		// if (b1.equals(b2)) return true;
		if (b1.getWidth() != b2.getWidth()) { return false; }
		if (b1.getHeight() != b2.getHeight()) { return false; }
		for (int y = 0; y < b1.getHeight(); y++) {
			for (int x = 0; x < b1.getWidth(); x++) {
				if (b1.getRGB(x, y) != b2.getRGB(x, y)) { return false; }
			}
		}
		return true;
	}
}
