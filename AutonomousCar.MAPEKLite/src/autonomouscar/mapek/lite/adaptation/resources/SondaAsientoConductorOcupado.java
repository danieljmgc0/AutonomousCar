package autonomouscar.mapek.lite.adaptation.resources;

import java.util.Objects;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.infraestructure.OSGiUtils;

public class SondaAsientoConductorOcupado extends Probe {

	public static String ID = "sonda-asiento-conductor-ocupado";

	private Thread worker = null;
	private volatile boolean running = false;
	private long periodMillis = 5000L;
	private Object lastReported = null;

	public SondaAsientoConductorOcupado(BundleContext context) {
		super(context, ID);
	}

	public SondaAsientoConductorOcupado setPeriodMillis(long ms) {
		this.periodMillis = ms;
		return this;
	}

	public void update() {
		IHumanSensors hs = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (hs == null)
			return;
		this.reportIfChanged(Boolean.valueOf(hs.isDriverSeatOccupied()));
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
		}, "SondaAsientoConductorOcupado-Worker");
		this.worker.setDaemon(true);
		this.worker.start();
	}

	public void stopMonitoring() {
		this.running = false;
		if (this.worker != null)
			this.worker.interrupt();
	}

	public void reportarMedicion(Boolean ocupado) {
		if (ocupado != null)
			this.reportIfChanged(ocupado);
	}

}
