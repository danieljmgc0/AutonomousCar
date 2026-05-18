package autonomouscar.mapek.lite.adaptation.resources;

import java.util.List;
import java.util.Objects;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IEngine;
import sua.autonomouscar.devices.interfaces.IRoadSensor;
import sua.autonomouscar.devices.interfaces.ISteering;
import sua.autonomouscar.driving.interfaces.IDrivingService;
import sua.autonomouscar.driving.interfaces.IL1_DrivingService;
import sua.autonomouscar.infraestructure.OSGiUtils;

public class SondaFalloCriticoSistema extends Probe {

	public static String ID = "sonda-fallo-critico-sistema";

	private Thread worker = null;
	private volatile boolean running = false;
	private long periodMillis = 5000L;
	private Object lastReported = null;

	public SondaFalloCriticoSistema(BundleContext context) {
		super(context, ID);
	}

	public SondaFalloCriticoSistema setPeriodMillis(long ms) {
		this.periodMillis = ms;
		return this;
	}

	public void update() {
		// Por defecto NO hay fallo crítico (estado seguro).
		// Sólo lo marcamos cuando un ADS L1+/L2/L3 está conduciendo activamente
		// y le falta motor, dirección o RoadSensor.
		Boolean resultado = Boolean.FALSE;

		List<IDrivingService> services = OSGiUtils.getServices(this.context, IDrivingService.class);
		boolean adsActivo = false;
		if (services != null) {
			for (IDrivingService s : services) {
				if (s == null)
					continue;
				if (!(s instanceof IL1_DrivingService))
					continue; // ignoramos L0 (modo seguro)
				if (s.isDriving()) {
					adsActivo = true;
					break;
				}
			}
		}

		if (adsActivo) {
			IEngine engine = OSGiUtils.getService(this.context, IEngine.class);
			ISteering steering = OSGiUtils.getService(this.context, ISteering.class);
			IRoadSensor road = OSGiUtils.getService(this.context, IRoadSensor.class);
			if (engine == null || steering == null || road == null)
				resultado = Boolean.TRUE;
		}

		this.reportIfChanged(resultado);
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
		}, "SondaFalloCriticoSistema-Worker");
		this.worker.setDaemon(true);
		this.worker.start();
	}

	public void stopMonitoring() {
		this.running = false;
		if (this.worker != null)
			this.worker.interrupt();
	}

	public void reportarMedicion(Boolean fallo) {
		if (fallo != null)
			this.reportIfChanged(fallo);
	}

}
