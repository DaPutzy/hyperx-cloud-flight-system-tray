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

	public BufferedImage loadImage(@NonNull String imagePath) {
		final URL resource = Objects.requireNonNull(getClass().getClassLoader().getResource(imagePath));

		return read(resource);
	}

	public BufferedImage loadImages(@NonNull List<String> imagePaths) {
		final List<BufferedImage> images = imagePaths.stream().map(this::loadImage).toList();

		return combineImagesHorizontally(images);
	}

	private BufferedImage combineImagesHorizontally(@NonNull List<BufferedImage> images) {
		if (images.isEmpty()) throw new RuntimeException("need at least one image to combine!");

		final BufferedImage image = new BufferedImage(
				images.stream().mapToInt(BufferedImage::getWidth).sum(),
				images.stream().mapToInt(BufferedImage::getHeight).max().orElseThrow(),
				BufferedImage.TYPE_4BYTE_ABGR
		);

		final Graphics2D graphics = image.createGraphics();

		int x = 0;
		for (final Image i : images) {
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
}
