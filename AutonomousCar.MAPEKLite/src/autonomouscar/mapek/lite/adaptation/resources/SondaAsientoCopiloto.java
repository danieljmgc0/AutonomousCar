package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class SondaAsientoCopiloto extends Probe implements ISimulationElement {

	public static String ID = "sonda-asiento-copiloto-ocupado";
	private BundleContext context;

	public SondaAsientoCopiloto(BundleContext context) {
		super(context, ID);
		this.context = context;
	}

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		IHumanSensors hs = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (hs != null)
			this.reportMeasure(hs.isCopilotSeatOccupied());
	}
}
