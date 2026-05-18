package autonomouscar.mapek.lite.adaptation.resources;

import java.util.Objects;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.interfaces.EFaceStatus;

public class SondaAtencionConductor extends Probe {

	public static String ID = "sonda-atencion-conductor";

	private Thread worker = null;
	private volatile boolean running = false;
	private long periodMillis = 5000L;
	private Object lastReported = null;

	public SondaAtencionConductor(BundleContext context) {
		super(context, ID);
	}

	public SondaAtencionConductor setPeriodMillis(long ms) {
		this.periodMillis = ms;
		return this;
	}

	public void update() {
		IHumanSensors hs = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (hs == null)
			return;
		EFaceStatus s = hs.getFaceStatus();
		if (s == null)
			return;
		this.reportIfChanged(s.name());
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
		}, "SondaAtencionConductor-Worker");
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
