package com.github.daputzy.hyperx_cloud_flight;

import com.github.daputzy.hyperx_cloud_flight.device.DeviceService;
import com.github.daputzy.hyperx_cloud_flight.tray.ImageService;
import com.github.daputzy.hyperx_cloud_flight.tray.TrayService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

	public static void main(String[] args) {
		// do not show java application in dock
		System.setProperty("apple.awt.UIElement", "true");

		// enable template images so macOS can handle image colors and theme cohesion
		System.setProperty("apple.awt.enableTemplateImages", "true");

		try {
			TrayService tray = new TrayService(new ImageService());
			tray.init();

			DeviceTrayEventHandler eventHandler = new DeviceTrayEventHandler(tray);

			DeviceService deviceManager = new DeviceService(eventHandler::handle);
			deviceManager.init();
		} catch (final Throwable e) {
			log.error("critical error", e);
			System.exit(1);
		}
	}
}
