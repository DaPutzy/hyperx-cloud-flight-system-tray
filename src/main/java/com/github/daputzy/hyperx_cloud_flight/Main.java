package com.github.daputzy.hyperx_cloud_flight;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.daputzy.hyperx_cloud_flight.device.DeviceDisconnectedException;
import com.github.daputzy.hyperx_cloud_flight.device.DeviceService;
import com.github.daputzy.hyperx_cloud_flight.tray.ImageService;
import com.github.daputzy.hyperx_cloud_flight.tray.TrayService;
import lombok.extern.slf4j.Slf4j;
import org.hid4java.HidDevice;

@Slf4j
public class Main {

	public static void main(String[] args) {
		// do not show java application in dock
		System.setProperty("apple.awt.UIElement", "true");

		// enable template images so macOS can handle image colors and theme cohesion
		System.setProperty("apple.awt.enableTemplateImages", "true");

		final Main main = new Main();

		try {
			main.init();
			main.start();
		} catch (final Throwable e) {
			log.error("critical error", e);
			System.exit(1);
		}
	}

	private DeviceService deviceService;

	private void init() throws Exception {
		TrayService tray = new TrayService(new ImageService());
		tray.init();

		DeviceTrayEventHandler eventHandler = new DeviceTrayEventHandler(tray);

		deviceService = new DeviceService(eventHandler::handle);
		deviceService.init();
	}

	private void start() throws Exception {
		if (deviceService == null) throw new IllegalStateException("device service not initialized");

		while (!Thread.interrupted()) {
			Optional<HidDevice> device;
			do {
				device = deviceService.scan();
				if (device.isEmpty()) {
					log.warn("cloud flight device not found");
					TimeUnit.SECONDS.sleep(5);
				}
			} while (!Thread.interrupted() && device.isEmpty());

			try {
				if (device.isPresent()) {
					deviceService.read(device.get());
				}
			} catch (final DeviceDisconnectedException e) {
				log.warn("cloud flight device disconnected");
			}
		}
	}
}
