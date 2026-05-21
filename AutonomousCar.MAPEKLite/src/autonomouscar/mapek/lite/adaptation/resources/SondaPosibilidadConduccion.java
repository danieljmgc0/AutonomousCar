package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.interfaces.IIdentifiable;
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
		// L2 está disponible si su ARC está registrado en OSGi (bundle arrancado) incluso si está desplegado como servicio activo.
		String filter = String.format("(%s=%s)", IIdentifiable.ID, "driving.L2.AdaptiveCruiseControl");
		boolean disponible = OSGiUtils.getService(this.context, IAdaptiveReadyComponent.class, filter) != null;
		this.reportMeasure(disponible);
	}
}
