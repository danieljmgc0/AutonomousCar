package autonomouscar.mapek.lite.adaptation.resources;

import java.util.Objects;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IRoadSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.interfaces.ERoadStatus;

public class SondaTraficoVia extends Probe {

	public static String ID = "sonda-trafico-via";

	private Thread worker = null;
	private volatile boolean running = false;
	private long periodMillis = 5000L;
	private Object lastReported = null;

	public SondaTraficoVia(BundleContext context) {
		super(context, ID);
	}

	public SondaTraficoVia setPeriodMillis(long ms) {
		this.periodMillis = ms;
		return this;
	}

	public void update() {
		IRoadSensor road = OSGiUtils.getService(this.context, IRoadSensor.class);
		if (road == null)
			return;
		ERoadStatus status = road.getRoadStatus();
		if (status == null)
			return;
		this.reportIfChanged(status.name());
	}

	private void reportIfChanged(Object value) {
		if (Objects.equals(value, this.lastReported))
			return;
		this.lastReported = value;
		this.reportMeasure(value);
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
		}, "SondaTraficoVia-Worker");
		this.worker.setDaemon(true);
		this.worker.start();
	}

	public void stopMonitoring() {
		this.running = false;
		if (this.worker != null)
			this.worker.interrupt();
	}

	public void reportarMedicion(String estado) {
		if (estado != null)
			this.reportIfChanged(estado.toUpperCase());
	}

}
