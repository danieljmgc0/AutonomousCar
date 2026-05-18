package autonomouscar.mapek.lite.adaptation.resources;

import java.util.List;
import java.util.Objects;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.driving.interfaces.IDrivingService;
import sua.autonomouscar.driving.interfaces.IL1_DrivingService;
import sua.autonomouscar.driving.interfaces.IL2_DrivingService;
import sua.autonomouscar.driving.interfaces.IL3_DrivingService;
import sua.autonomouscar.infraestructure.OSGiUtils;

public class SondaNivelAutonomia extends Probe {

	public static String ID = "sonda-nivel-autonomia";

	private Thread worker = null;
	private volatile boolean running = false;
	private long periodMillis = 5000L;
	private Object lastReported = null;

	public SondaNivelAutonomia(BundleContext context) {
		super(context, ID);
	}

	public SondaNivelAutonomia setPeriodMillis(long ms) {
		this.periodMillis = ms;
		return this;
	}

	public void update() {
		List<IDrivingService> services = OSGiUtils.getServices(this.context, IDrivingService.class);
		int nivel = 0;

		if (services != null) {
			for (IDrivingService s : services) {
				if (s == null || !s.isDriving())
					continue;
				int lvl = levelOf(s);
				if (lvl > nivel)
					nivel = lvl;
			}
		}

		this.reportIfChanged(Integer.valueOf(nivel));
	}

	private void reportIfChanged(Object value) {
		if (Objects.equals(value, this.lastReported))
			return;
		this.lastReported = value;
		this.reportMeasure(value);
	}

	private int levelOf(IDrivingService s) {
		if (s instanceof IL3_DrivingService)
			return 3;
		if (s instanceof IL2_DrivingService)
			return 2;
		if (s instanceof IL1_DrivingService)
			return 1;
		return 0;
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
		}, "SondaNivelAutonomia-Worker");
		this.worker.setDaemon(true);
		this.worker.start();
	}

	public void stopMonitoring() {
		this.running = false;
		if (this.worker != null)
			this.worker.interrupt();
	}

	public void reportarMedicion(Integer nivel) {
		if (nivel != null)
			this.reportIfChanged(nivel);
	}

}
