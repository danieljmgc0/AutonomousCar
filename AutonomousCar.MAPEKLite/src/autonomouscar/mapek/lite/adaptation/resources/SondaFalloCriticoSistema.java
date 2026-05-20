package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IEngine;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.devices.interfaces.IRoadSensor;
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
		IDrivingService ds = OSGiUtils.getService(this.context, IDrivingService.class,
				String.format("(%s=true)", DrivingService.ACTIVE));

		// Con L0 activo no hay fallo crítico que gestionar
		if (ds != null && ds.getId().startsWith("L0_")) {
			this.reportMeasure(false);
			return;
		}

		// El motor es el indicador de que el sistema ya fue inicializado
		boolean motorOk = OSGiUtils.getService(this.context, IEngine.class) != null;

		// Sin servicio activo: si el sistema YA está inicializado (motor desplegado)
		// pero no hay ningún servicio conduciendo → fallo crítico (no existe configuración posible)
		if (ds == null) {
			this.reportMeasure(motorOk);
			return;
		}

		// Con servicio L1/L2/L3 activo: fallo si el hardware crítico no responde
		boolean dirOk = OSGiUtils.getService(this.context, ISteering.class) != null;
		if (!motorOk || !dirOk) {
			this.reportMeasure(true);
			return;
		}

		// Para servicios L3 también son críticos HumanSensors y RoadSensor
		if (ds.getId().startsWith("L3_")) {
			boolean humanSensorsOk = OSGiUtils.getService(this.context, IHumanSensors.class) != null;
			boolean roadSensorOk   = OSGiUtils.getService(this.context, IRoadSensor.class) != null;
			this.reportMeasure(!humanSensorsOk || !roadSensorOk);
			return;
		}

		this.reportMeasure(false);
	}
}
