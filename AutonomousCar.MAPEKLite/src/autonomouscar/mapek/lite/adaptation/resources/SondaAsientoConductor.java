package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class SondaAsientoConductor extends Probe implements ISimulationElement {

	public static String ID = "sonda-asiento-conductor-ocupado";
	private BundleContext context;

	public SondaAsientoConductor(BundleContext context) {
		super(context, ID);
		this.context = context;
	}

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		IHumanSensors hs = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (hs != null)
			this.reportMeasure(hs.isDriverSeatOccupied());
	}
}
