package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IEngine;
import sua.autonomouscar.devices.interfaces.ISteering;
import sua.autonomouscar.driving.interfaces.IDrivingService;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.infraestructure.driving.DrivingService;
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
		// Solo es un fallo crítico si hay un servicio L2+ activo y le faltan componentes
		IDrivingService ds = OSGiUtils.getService(this.context, IDrivingService.class,
				String.format("(%s=true)", DrivingService.ACTIVE));

		if (ds == null || ds.getId().startsWith("L0_") || ds.getId().startsWith("L1_")) {
			this.reportMeasure(false);
			return;
		}

		boolean motorOk = OSGiUtils.getService(this.context, IEngine.class) != null;
		boolean dirOk   = OSGiUtils.getService(this.context, ISteering.class) != null;
		this.reportMeasure(!motorOk || !dirOk);
	}
}
