package com.github.daputzy.hyperx_cloud_flight.device;

import com.github.daputzy.hyperx_cloud_flight.device.Event.BatteryLevel;
import com.github.daputzy.hyperx_cloud_flight.device.Event.Ignore;
import com.github.daputzy.hyperx_cloud_flight.device.Event.Muted;
import com.github.daputzy.hyperx_cloud_flight.device.Event.PowerOn;
import com.github.daputzy.hyperx_cloud_flight.device.Event.VolumeDown;
import com.github.daputzy.hyperx_cloud_flight.device.Event.VolumeUp;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hid4java.HidDevice;
import org.hid4java.HidException;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import org.hid4java.ScanMode;
import org.hid4java.jna.HidApi;

@Slf4j
@RequiredArgsConstructor
public class DeviceService {

	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private static final int VENDOR_ID = 0x0951;
	private static final int PRODUCT_ID = 0x16c4;

	private static final boolean REFRESH_BATTERY_ON_MUTE = true;

	private final Consumer<Event> eventConsumer;

	public void init() throws HidException {
		HidApi.darwinOpenDevicesNonExclusive = true;

		HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
		hidServicesSpecification.setScanMode(ScanMode.NO_SCAN);
		hidServicesSpecification.setAutoStart(false);
		hidServicesSpecification.setAutoDataRead(false);
		hidServicesSpecification.setAutoShutdown(true);

		HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
		hidServices.start();

		HidDevice device = hidServices.getAttachedHidDevices().stream()
				.filter(d -> Objects.equals(VENDOR_ID, d.getVendorId()) && Objects.equals(PRODUCT_ID, d.getProductId()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("did not find hyperx cloud flight device"));

		device.open();
		device.setNonBlocking(false);

		scheduler.scheduleAtFixedRate(() -> triggerBatteryLevel(device), 0, 5, TimeUnit.MINUTES);

		while (!Thread.interrupted()) {
			final byte[] data = new byte[32];
			final int size = device.read(data);
			Event event = parseResult(size, data);

			if (REFRESH_BATTERY_ON_MUTE && event instanceof Muted) triggerBatteryLevel(device);
			if (event instanceof PowerOn) triggerBatteryLevel(device);

			logEvent(event);
			eventConsumer.accept(event);
		}
	}

	private void triggerBatteryLevel(HidDevice device) {
		byte[] packet = new byte[2];
		packet[0] = (byte) 0xff;
		packet[1] = (byte) 0x05;

		int writtenBytes = device.write(packet, 19, (byte) 0x21, true);

		if (writtenBytes < 0) throw new RuntimeException("could not write");
	}

	private Event parseResult(int size, byte[] data) {
		switch (size) {
			case 2:
				if (data[0] == 0x64) {
					if (data[1] == 0x01) {
						return Event.POWER_ON;
					} else if (data[1] == 0x03){
						return Event.POWER_OFF;
					}
				}
				if (data[0] == 0x65) {
					if (data[1] == 0x04) {
						return Event.MUTED;
					}
					return Event.UN_MUTED;
				}
				break;

			case 5:
				if (data[1] == 0x01) {
					return Event.VOLUME_UP;
				} else if (data[1] == 0x02){
					return Event.VOLUME_DOWN;
				}
				break;

			case 20:
				if (data[3] == 0x10 || data[3] == 0x11) {
					if ((data[4] & 0xff) >= 20) {
						return Event.BATTERY_CHARGING;
					}
					return Event.BatteryLevel.of(100);
				}
				Integer level = parseBatteryPercent(data[3], data[4] & 0xff);
				return Event.BatteryLevel.of(level);

			default:
				return Event.IGNORE;
		}

		return Event.IGNORE;
	}

	private Integer parseBatteryPercent(Byte state, int value) {
		if (state == 0x0e) {
			if (value >= 0 && value < 90) return 10;
			if (value >= 90 && value < 120) return 15;
			if (value >= 120 && value < 149) return 20;
			if (value >= 149 && value < 160) return 25;
			if (value >= 160 && value < 170) return 30;
			if (value >= 170 && value < 180) return 35;
			if (value >= 180 && value < 190) return 40;
			if (value >= 190 && value < 200) return 45;
			if (value >= 200 && value < 210) return 50;
			if (value >= 210 && value < 220) return 55;
			if (value >= 220 && value < 240) return 60;
			if (value >= 240 && value <= 255) return 65;
		} else if (state == 0x0f) {
			if (value >= 0 && value < 20) return 70;
			if (value >= 20 && value < 50) return 75;
			if (value >= 50 && value < 70) return 80;
			if (value >= 70 && value < 100) return 85;
			if (value >= 100 && value < 120) return 90;
			if (value >= 120 && value < 130) return 95;
			if (value >= 130 && value <= 255) return 100;
		}

		return 0;
	}

	private void logEvent(Event event) {
		if (event instanceof BatteryLevel batteryLevelEvent) {
			log.debug("{}: {}", event.getClass().getSimpleName(), batteryLevelEvent.getLevel());
		} else if (event instanceof Ignore || event instanceof VolumeUp || event instanceof VolumeDown) {
			log.trace("{}", event.getClass().getSimpleName());
		} else {
			log.debug("{}", event.getClass().getSimpleName());
		}
	}
}
