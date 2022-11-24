package com.github.daputzy.hyperx_cloud_flight.tray;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class ImageService {

	public Image loadImage(@NonNull String imagePath) {
		URL resource = Objects.requireNonNull(getClass().getClassLoader().getResource(imagePath));

		return read(resource);
	}

	public Image loadImages(@NonNull List<String> imageFiles) {
		List<BufferedImage> images = imageFiles.stream()
				.filter(Objects::nonNull)
				.map(getClass().getClassLoader()::getResource)
				.filter(Objects::nonNull)
				.map(this::read)
				.toList();

		return combineImagesHorizontally(images);
	}

	private Image combineImagesHorizontally(@NonNull List<BufferedImage> images) {
		if (images.isEmpty()) throw new RuntimeException("need at least one image to combine!");

		BufferedImage image = new BufferedImage(
				images.stream().mapToInt(BufferedImage::getWidth).sum(),
				images.stream().mapToInt(BufferedImage::getHeight).max().orElseThrow(),
				BufferedImage.TYPE_4BYTE_ABGR
		);

		Graphics2D graphics = image.createGraphics();

		int x = 0;
		for (Image i : images) {
			graphics.drawImage(i, x, 0, null);
			x += i.getWidth(null);
		}

		graphics.dispose();

		return image;
	}

	@SneakyThrows(IOException.class)
	private BufferedImage read(@NonNull URL url) {
		return ImageIO.read(url);
	}

//	private void invertImage(BufferedImage image) {
//		DataBufferByte buf = (DataBufferByte) image.getRaster().getDataBuffer();
//		byte[] values = buf.getData();
//		for (int i = 0; i < values.length; i ++) {
//			if (i % 4 != 0) {
//				values[i] = (byte) (values[i] ^ 0xff);
//			}
//		}
//	}
}
