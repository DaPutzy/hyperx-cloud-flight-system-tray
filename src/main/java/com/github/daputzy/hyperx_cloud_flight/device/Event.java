package com.github.daputzy.hyperx_cloud_flight.device;

public interface Event {

	Event MUTED = new Muted();
	Event UN_MUTED = new UnMuted();
	Event VOLUME_UP = new VolumeUp();
	Event VOLUME_DOWN = new VolumeDown();
	Event POWER_ON = new PowerOn();
	Event POWER_OFF = new PowerOff();
	Event IGNORE = new Ignore();
	Event BATTERY_CHARGING = new BatteryCharging();

	class Muted implements Event {}
	class UnMuted implements Event {}
	class VolumeUp implements Event {}
	class VolumeDown implements Event {}
	class PowerOn implements Event {}
	class PowerOff implements Event {}
	class Ignore implements Event {}
	class BatteryCharging implements Event {}

	class BatteryLevel implements Event {
		private final Integer level;

		public Integer getLevel() {
			return level;
		}

		private BatteryLevel(Integer level) {
			this.level = level;
		}

		public static BatteryLevel of(Integer level) {
			return new BatteryLevel(level);
		}
	}
}
