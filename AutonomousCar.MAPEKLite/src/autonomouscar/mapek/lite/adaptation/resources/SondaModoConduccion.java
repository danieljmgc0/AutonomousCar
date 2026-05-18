package autonomouscar.mapek.lite.adaptation.resources;

import java.util.List;
import java.util.Objects;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.driving.interfaces.IDrivingService;
import sua.autonomouscar.driving.interfaces.IL0_ManualDriving;
import sua.autonomouscar.driving.interfaces.IL1_AssistedDriving;
import sua.autonomouscar.driving.interfaces.IL2_AdaptiveCruiseControl;
import sua.autonomouscar.driving.interfaces.IL2_LaneKeepingAssist;
import sua.autonomouscar.driving.interfaces.IL3_CityChauffer;
import sua.autonomouscar.driving.interfaces.IL3_HighwayChauffer;
import sua.autonomouscar.driving.interfaces.IL3_TrafficJamChauffer;
import sua.autonomouscar.infraestructure.OSGiUtils;

public class SondaModoConduccion extends Probe {

	public static String ID = "sonda-modo-conduccion";

	private Thread worker = null;
	private volatile boolean running = false;
	private long periodMillis = 5000L;
	private Object lastReported = null;

	public SondaModoConduccion(BundleContext context) {
		super(context, ID);
	}

	public SondaModoConduccion setPeriodMillis(long ms) {
		this.periodMillis = ms;
		return this;
	}

	public void update() {
		List<IDrivingService> services = OSGiUtils.getServices(this.context, IDrivingService.class);
		String modo = "L0_M";

		if (services != null) {
			for (IDrivingService s : services) {
				if (s == null || !s.isDriving())
					continue;
				modo = classifyMode(s);
				if (!"L0_M".equals(modo))
					break;
			}
		}

		this.reportIfChanged(modo);
	}

	private void reportIfChanged(Object value) {
		if (Objects.equals(value, this.lastReported))
			return;
		this.lastReported = value;
		this.reportMeasure(value);
	}

	private String classifyMode(IDrivingService s) {
		if (s instanceof IL3_HighwayChauffer)
			return "L3_HighwayChauffer";
		if (s instanceof IL3_TrafficJamChauffer)
			return "L3_TrafficJamChauffer";
		if (s instanceof IL3_CityChauffer)
			return "L3_CityChauffer";
		if (s instanceof IL2_AdaptiveCruiseControl)
			return "L2_AdaptiveCruiseControl";
		if (s instanceof IL2_LaneKeepingAssist)
			return "L2_LaneKeepingAssist";
		if (s instanceof IL1_AssistedDriving)
			return "L1_AssistedDriving";
		if (s instanceof IL0_ManualDriving)
			return "L0_ManualDriving";
		return "L0_M";
	}

	public void startMonitoring() {
		if (this.running)
			return;
		this.running = true;
		this.worker = new Thread(() -> {
			while (this.running) {
				try {
					this.update();
					Thread.sleep(this.periodMillis);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					return;
				} catch (Exception e) {
				}
			}
		}, "SondaModoConduccion-Worker");
		this.worker.setDaemon(true);
		this.worker.start();
	}

	public void stopMonitoring() {
		this.running = false;
		if (this.worker != null)
			this.worker.interrupt();
	}

	public void reportarMedicion(String modo) {
		if (modo != null)
			this.reportIfChanged(modo);
	}

}
