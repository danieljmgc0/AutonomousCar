package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IRoadSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class SondaTraficoVia extends Probe implements ISimulationElement {

	public static String ID = "sonda-trafico-via";
	private BundleContext context;

	public SondaTraficoVia(BundleContext context) {
		super(context, ID);
		this.context = context;
	}

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		IRoadSensor rs = OSGiUtils.getService(this.context, IRoadSensor.class);
		if (rs != null)
			this.reportMeasure(rs.getRoadStatus());
	}
}
