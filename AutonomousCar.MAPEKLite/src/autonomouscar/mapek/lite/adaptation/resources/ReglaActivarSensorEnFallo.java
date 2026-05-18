package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.AdaptationRule;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.exceptions.analyzing.RuleException;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.structures.systemconfiguration.interfaces.IRuleSystemConfiguration;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;
import sua.autonomouscar.devices.interfaces.IDistanceSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.infraestructure.devices.ARC.DistanceSensorARC;
import sua.autonomouscar.infraestructure.driving.ARC.L2_DrivingServiceARC;
import sua.autonomouscar.interfaces.IIdentifiable;

/**
 * ADS_L3-7 / ADS-1: nivel-autonomia==3 y RightDistanceSensor no disponible.
 * Si LIDAR disponible → sustitución. Sino → quitar L3 (fallo crítico).
 */
public class ReglaActivarSensorEnFallo extends AdaptationRule {

	public static String ID = "regla-activar-sensor-en-fallo-sensor1";

	private BundleContext context;
	IKnowledgeProperty kp_nivel;
	IKnowledgeProperty kp_modo;

	public ReglaActivarSensorEnFallo(BundleContext context) {
		super(context, ID);
		this.context = context;
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		kp_nivel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_modo  = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivel == null || kp_nivel.getValue() == null) return false;
		if ((Integer) kp_nivel.getValue() != 3) return false;
		// Verificar que RightDistanceSensor no está disponible
		IDistanceSensor rds = OSGiUtils.getService(context, IDistanceSensor.class,
				"(" + IIdentifiable.ID + "=RightDistanceSensor)");
		return rds == null;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Comprobar si el LIDAR está disponible
		IDistanceSensor lidarRight = OSGiUtils.getService(context, IDistanceSensor.class,
				"(" + IIdentifiable.ID + "=LIDAR-RightDistanceSensor)");

		String dsActivo = kp_modo != null ? (String) kp_modo.getValue() : null;

		if (lidarRight != null && dsActivo != null) {
			// Añadir LIDAR y redirigir el binding de RightDistanceSensor
			SystemConfigurationHelper.componentToAdd(config, "device.LIDAR.RightDistanceSensor", "1.0.0");
			SystemConfigurationHelper.bindingToRemove(config,
					dsActivo, "1.0.0", L2_DrivingServiceARC.REQUIRED_RIGHTDISTANCESENSOR,
					"device.RightDistanceSensor", "1.0.0", DistanceSensorARC.PROVIDED_SENSOR);
			SystemConfigurationHelper.bindingToAdd(config,
					dsActivo, "1.0.0", L2_DrivingServiceARC.REQUIRED_RIGHTDISTANCESENSOR,
					"device.LIDAR.RightDistanceSensor", "1.0.0", DistanceSensorARC.PROVIDED_SENSOR);
		} else if (dsActivo != null) {
			// Sin redundancia → quitar el servicio L3 activo (el monitor lo detectará)
			SystemConfigurationHelper.componentToRemove(config, dsActivo, "1.0.0");
		}
		return config;
	}
}
