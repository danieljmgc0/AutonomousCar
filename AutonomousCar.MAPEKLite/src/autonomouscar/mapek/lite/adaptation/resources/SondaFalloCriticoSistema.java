package autonomouscar.mapek.lite.adaptation.resources;

import java.util.List;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.devices.interfaces.IRoadSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class SondaFalloCriticoSistema extends Probe implements ISimulationElement {

	public static String ID = "sonda-fallo-critico-sistema";
	private BundleContext context;

	public SondaFalloCriticoSistema(BundleContext context) {
		super(context, ID);
		this.context = context;
	}

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		// Comprobar si alguna sonda del bucle MAPE-K no está arrancada.
		if (haySondaInactiva()) {
			this.reportMeasure(true);
			return;
		}

		// Comprobar si falta algún sensor crítico que  observe el entorno o al conductor.
		boolean roadSensorOk   = OSGiUtils.getService(this.context, IRoadSensor.class)   != null;
		boolean humanSensorsOk = OSGiUtils.getService(this.context, IHumanSensors.class) != null;

		this.reportMeasure(!roadSensorOk || !humanSensorsOk);
	}

	private boolean haySondaInactiva() {
		List<ISimulationElement> elementos = OSGiUtils.getServices(this.context, ISimulationElement.class);
		if (elementos == null) return false;
		for (ISimulationElement el : elementos) {
			if (el == this) continue;
			if (el instanceof Probe) {
				Probe sonda = (Probe) el;
				if (!sonda.isStarted()) {
					this.logger.warn(String.format("Sonda %s no está arrancada → fallo crítico", sonda.getId()));
					return true;
				}
			}
		}
		return false;
	}
}
