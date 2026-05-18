package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.driving.interfaces.IDrivingService;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.infraestructure.driving.DrivingService;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class SondaModoConduccion extends Probe implements ISimulationElement {

	public static String ID = "sonda-modo-conduccion";
	private BundleContext context;

	public SondaModoConduccion(BundleContext context) {
		super(context, ID);
		this.context = context;
	}

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		IDrivingService ds = OSGiUtils.getService(
				this.context, IDrivingService.class,
				String.format("(%s=true)", DrivingService.ACTIVE));
		String serviceId = (ds != null) ? ds.getId() : "L0_ManualDriving";
		this.reportMeasure(serviceId);
	}
}
