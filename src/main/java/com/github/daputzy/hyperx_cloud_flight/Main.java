package com.github.daputzy.hyperx_cloud_flight;

import com.github.daputzy.hyperx_cloud_flight.device.DeviceService;
import com.github.daputzy.hyperx_cloud_flight.tray.ImageService;
import com.github.daputzy.hyperx_cloud_flight.tray.TrayService;
import java.awt.AWTException;

public class Main {

	public static void main(String[] args) throws AWTException {
		// do not show java application in dock
		System.setProperty("apple.awt.UIElement", "true");

		// enable template images so macOS can handle image colors and theme cohesion
		System.setProperty("apple.awt.enableTemplateImages", "true");

		TrayService tray = new TrayService(new ImageService());
		tray.init();

		DeviceTrayEventHandler eventHandler = new DeviceTrayEventHandler(tray);

		DeviceService deviceManager = new DeviceService(eventHandler::handle);
		deviceManager.init();
	}
}
