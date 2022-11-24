package com.github.daputzy.hyperx_cloud_flight;

import com.github.daputzy.hyperx_cloud_flight.device.Event;
import com.github.daputzy.hyperx_cloud_flight.device.Event.BatteryCharging;
import com.github.daputzy.hyperx_cloud_flight.device.Event.BatteryLevel;
import com.github.daputzy.hyperx_cloud_flight.device.Event.Muted;
import com.github.daputzy.hyperx_cloud_flight.device.Event.PowerOff;
import com.github.daputzy.hyperx_cloud_flight.device.Event.PowerOn;
import com.github.daputzy.hyperx_cloud_flight.device.Event.UnMuted;
import com.github.daputzy.hyperx_cloud_flight.tray.TrayService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeviceTrayEventHandler {

	private final TrayService tray;

	private boolean muted = false;
	private Integer batteryPercentage = null;

	public void handle(Event event) {
		if (event instanceof Muted) {
			muted = true;
			tray.setPowerLabelValue(true);
			tray.setMutedLabelValue(true);
		} else if (event instanceof UnMuted) {
			muted = false;
			tray.setPowerLabelValue(true);
			tray.setMutedLabelValue(false);
		} else if (event instanceof PowerOn) {
			tray.setPowerLabelValue(true);
		} else if (event instanceof PowerOff) {
			muted = false;
			batteryPercentage = null;
			tray.setPowerLabelValue(false);
			tray.setMutedLabelValue(null);
			tray.setBatteryLabelValue(null);
		} else if (event instanceof BatteryLevel batteryEvent) {
			batteryPercentage = batteryEvent.getLevel();
			tray.setPowerLabelValue(true);
			tray.setBatteryLabelValue(batteryPercentage);
		} else if (event instanceof BatteryCharging) {
			batteryPercentage = TrayService.BATTERY_LEVEL_CHARGING;
			tray.setPowerLabelValue(true);
			tray.setBatteryLabelValue(TrayService.BATTERY_LEVEL_CHARGING);
		}

		tray.setImages(muted, batteryPercentage);
	}
}
