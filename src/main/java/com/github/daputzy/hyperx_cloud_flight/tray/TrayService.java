package com.github.daputzy.hyperx_cloud_flight.tray;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrayService {

	private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public static final int BATTERY_LEVEL_CHARGING = -1;

	private static final String ICON_BAT_00_010 = "img/battery_0-10.png";
	private static final String ICON_BAT_10_020 = "img/battery_10-20.png";
	private static final String ICON_BAT_20_030 = "img/battery_20-30.png";
	private static final String ICON_BAT_30_040 = "img/battery_30-40.png";
	private static final String ICON_BAT_40_055 = "img/battery_40-55.png";
	private static final String ICON_BAT_55_070 = "img/battery_55-70.png";
	private static final String ICON_BAT_70_085 = "img/battery_70-85.png";
	private static final String ICON_BAT_85_100 = "img/battery_85-100.png";
	private static final String ICON_BAT_UNKNOWN = "img/battery_unknown.png";
	private static final String ICON_BAT_CHARGING = "img/battery_charging.png";

	private static final String ICON_HEADSET_UN_MUTED = "img/headset_un_muted.png";
	private static final String ICON_HEADSET_MUTED = "img/headset_muted.png";

	private static final String LABEL_POWER = "Power: ";
	private static final String LABEL_MUTED = "Muted: ";
	private static final String LABEL_BATTERY = "Battery: ";

	private final ImageService imageService;
	private final SystemTray systemTray;

	private final PopupMenu menu;
	private final TrayIcon trayIcon;

	private final MenuItem powerMenuItem;
	private final MenuItem mutedMenuItem;
	private final MenuItem batteryMenuItem;

	public TrayService(ImageService imageService) {
		if (!SystemTray.isSupported()) throw new IllegalStateException("system tray is not supported!");

		this.imageService = imageService;
		systemTray = SystemTray.getSystemTray();

		menu = new PopupMenu();

		// load simple image here synchronously because TrayIcon requires one and load complex images later asynchronously
		trayIcon = new TrayIcon(imageService.loadImage(ICON_HEADSET_UN_MUTED), "HyperX Cloud Flight", menu);

		powerMenuItem = new MenuItem();
		powerMenuItem.setEnabled(false);
		setPowerLabelValue(null);

		mutedMenuItem = new MenuItem();
		mutedMenuItem.setEnabled(false);
		setMutedLabelValue(null);

		batteryMenuItem = new MenuItem();
		batteryMenuItem.setEnabled(false);
		setBatteryLabelValue(null);
	}

	public void init() throws AWTException {
		systemTray.add(trayIcon);

		MenuItem quit = new MenuItem("Quit");
		quit.addActionListener((e) -> System.exit(0));

		menu.add(powerMenuItem);
		menu.add(mutedMenuItem);
		menu.add(batteryMenuItem);
		menu.addSeparator();
		menu.add(quit);

		setImages(ICON_HEADSET_UN_MUTED, ICON_BAT_UNKNOWN);
	}

	public void setPowerLabelValue(Boolean power) {
		String value = LABEL_POWER;

		if (power == null) value += "N/A";
		else if (power) value += "On";
		else value += "Off";

		powerMenuItem.setLabel(value);
	}

	public void setMutedLabelValue(Boolean muted) {
		String value = LABEL_MUTED;

		if (muted == null) value += "N/A";
		else if (muted) value += "Yes";
		else value += "No";

		mutedMenuItem.setLabel(value);
	}

	/**
	 * @param level NULL => N/A
	 * 				-1   => Charging
	 * 				>= 0 => %
	 */
	public void setBatteryLabelValue(Integer level) {
		String value = LABEL_BATTERY;

		if (level == null) value += "N/A";
		else if (level == BATTERY_LEVEL_CHARGING) value += "Charging";
		else value += level + "%";

		batteryMenuItem.setLabel(value);
	}

	/**
	 *
	 * @param muted is muted
	 * @param batteryLevel NULL => N/A
	 *                     -1   => Charging
	 *                     >= 0 => %
	 */
	public void setImages(boolean muted, Integer batteryLevel) {
		String headsetIcon = (muted) ? ICON_HEADSET_MUTED : ICON_HEADSET_UN_MUTED;
		String batteryIcon = ICON_BAT_UNKNOWN;

		if (batteryLevel != null) {
			if (batteryLevel == BATTERY_LEVEL_CHARGING) batteryIcon = ICON_BAT_CHARGING;
			else if (batteryLevel >= 0 && batteryLevel < 10) batteryIcon = ICON_BAT_00_010;
			else if (batteryLevel >= 10 && batteryLevel < 20) batteryIcon = ICON_BAT_10_020;
			else if (batteryLevel >= 20 && batteryLevel < 30) batteryIcon = ICON_BAT_20_030;
			else if (batteryLevel >= 30 && batteryLevel < 40) batteryIcon = ICON_BAT_30_040;
			else if (batteryLevel >= 40 && batteryLevel < 55) batteryIcon = ICON_BAT_40_055;
			else if (batteryLevel >= 55 && batteryLevel < 70) batteryIcon = ICON_BAT_55_070;
			else if (batteryLevel >= 70 && batteryLevel < 85) batteryIcon = ICON_BAT_70_085;
			else if (batteryLevel >= 85) batteryIcon = ICON_BAT_85_100;
		}

		setImages(headsetIcon, batteryIcon);
	}

	private void setImages(String headsetIcon, String batteryIcon) {
		executorService.submit(() -> trayIcon.setImage(imageService.loadImages(List.of(headsetIcon, batteryIcon))));
	}
}
