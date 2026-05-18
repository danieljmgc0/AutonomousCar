package autonomouscar.mapek.lite.adaptation.resources;

import java.util.Objects;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IRoadSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.interfaces.ERoadType;

public class SondaTipoVia extends Probe {

	public static String ID = "sonda-tipo-via";

	private Thread worker = null;
	private volatile boolean running = false;
	private long periodMillis = 5000L;
	private Object lastReported = null;

	public SondaTipoVia(BundleContext context) {
		super(context, ID);
	}

	public SondaTipoVia setPeriodMillis(long ms) {
		this.periodMillis = ms;
		return this;
	}

	public void update() {
		IRoadSensor road = OSGiUtils.getService(this.context, IRoadSensor.class);
		if (road == null)
			return;
		ERoadType type = road.getRoadType();
		if (type == null)
			return;
		this.reportIfChanged(type.name());
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
		}, "SondaTipoVia-Worker");
		this.worker.setDaemon(true);
		this.worker.start();
	}

	public void stopMonitoring() {
		this.running = false;
		if (this.worker != null)
			this.worker.interrupt();
	}

	public void reportarMedicion(String tipo) {
		if (tipo != null)
			this.reportIfChanged(tipo.toUpperCase());
	}

}
