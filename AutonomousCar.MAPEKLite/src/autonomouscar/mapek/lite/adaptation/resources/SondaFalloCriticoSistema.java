package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IEngine;
import sua.autonomouscar.devices.interfaces.ISteering;
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
		boolean motorDisponible = OSGiUtils.getService(this.context, IEngine.class) != null;
		boolean dirDisponible = OSGiUtils.getService(this.context, ISteering.class) != null;
		// fallo-critico = true cuando algún componente crítico no está disponible
		boolean falloCritico = !motorDisponible || !dirDisponible;
		this.reportMeasure(falloCritico);
	}
}
