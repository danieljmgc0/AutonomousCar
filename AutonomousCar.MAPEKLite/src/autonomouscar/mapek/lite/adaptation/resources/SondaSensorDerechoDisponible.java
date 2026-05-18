package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IDistanceSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.interfaces.IIdentifiable;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class SondaSensorDerechoDisponible extends Probe implements ISimulationElement {

	public static String ID = "sonda-sensor-derecho-disponible";
	private BundleContext context;

	public SondaSensorDerechoDisponible(BundleContext context) {
		super(context, ID);
		this.context = context;
	}

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		String filter = String.format("(%s=RightDistanceSensor)", IIdentifiable.ID);
		boolean disponible = OSGiUtils.getService(this.context, IDistanceSensor.class, filter) != null;
		this.reportMeasure(disponible);
	}
}
