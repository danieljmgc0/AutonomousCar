package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import java.util.List;

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
		// Buscar todos los servicios activos y seleccionar el de mayor nivel (L3>L2>L1>L0)
		List<IDrivingService> activos = OSGiUtils.getServices(
				this.context, IDrivingService.class,
				String.format("(%s=true)", DrivingService.ACTIVE));

		String serviceId = "L0_ManualDriving";
		if (activos != null) {
			for (IDrivingService ds : activos) {
				String id = ds.getId();
				if (id.startsWith("L3_")) { serviceId = id; break; }
				if (id.startsWith("L2_") && !serviceId.startsWith("L3_")) serviceId = id;
				if (id.startsWith("L1_") && !serviceId.startsWith("L3_") && !serviceId.startsWith("L2_")) serviceId = id;
			}
		}
		this.reportMeasure(serviceId);
	}
}
