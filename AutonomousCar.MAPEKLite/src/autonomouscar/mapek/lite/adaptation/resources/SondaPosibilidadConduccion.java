package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.driving.interfaces.IL2_AdaptiveCruiseControl;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class SondaPosibilidadConduccion extends Probe implements ISimulationElement {

	public static String ID = "sonda-posibilidad-conduccion";
	private BundleContext context;

	public SondaPosibilidadConduccion(BundleContext context) {
		super(context, ID);
		this.context = context;
	}

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		boolean disponible = OSGiUtils.getService(this.context, IL2_AdaptiveCruiseControl.class) != null;
		this.reportMeasure(disponible);
	}
}
