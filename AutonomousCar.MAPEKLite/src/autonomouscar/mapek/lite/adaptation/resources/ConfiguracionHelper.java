package autonomouscar.mapek.lite.adaptation.resources;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;

/**
 * Helpers para describir cambios de configuración (componentes a añadir/quitar
 * y bindings esenciales) reutilizados por las reglas de adaptación. Centraliza
 * los IDs de componentes y de interfaces para evitar typos entre reglas.
 */
public final class ConfiguracionHelper {

	private ConfiguracionHelper() {
	}

	public static final String V = "1.0.0";

	// Component IDs - dispositivos
	public static final String DEV_ENGINE = "device.Engine";
	public static final String DEV_STEERING = "device.Steering";
	public static final String DEV_ROADSENSOR = "device.RoadSensor";
	public static final String DEV_HUMANSENSORS = "device.HumanSensors";
	public static final String DEV_FRONT_DIST = "device.FrontDistanceSensor";
	public static final String DEV_REAR_DIST = "device.RearDistanceSensor";
	public static final String DEV_LEFT_DIST = "device.LeftDistanceSensor";
	public static final String DEV_RIGHT_DIST = "device.RightDistanceSensor";
	public static final String DEV_LEFT_LINE = "device.LeftLineSensor";
	public static final String DEV_RIGHT_LINE = "device.RightLineSensor";
	public static final String DEV_LIDAR = "device.LIDAR";

	// Component IDs - conducción
	public static final String DRV_L0_MANUAL = "driving.L0.ManualDriving";
	public static final String DRV_L1_ASSISTED = "driving.L1.AssistedDriving";
	public static final String DRV_L2_ACC = "driving.L2.AdaptiveCruiseControl";
	public static final String DRV_L2_LKA = "driving.L2.LaneKeepingAssist";
	public static final String DRV_L3_HIGHWAY = "driving.L3.HighwayChauffer";
	public static final String DRV_L3_TRAFFICJAM = "driving.L3.TrafficJamChauffer";
	public static final String DRV_L3_CITY = "driving.L3.CityChauffer";
	public static final String FB_EMERGENCY = "driving.FallbackPlan.Emergency";
	public static final String FB_PARK = "driving.FallbackPlan.ParkInTheRoadShoulder";

	// Component IDs - interacción
	public static final String INT_NOTIFICATION = "interaction.NotificationService";
	public static final String INT_STEERINGWHEEL = "interaction.SteeringWheel";
	public static final String INT_SEAT_DRIVER = "interaction.Seat.Driver";
	public static final String INT_SEAT_COPILOT = "interaction.Seat.Copilot";
	public static final String INT_DRIVER_TEXT = "interaction.DriverDisplay.VisualText";
	public static final String INT_DRIVER_ICON = "interaction.DriverDisplay.VisualIcon";
	public static final String INT_DASH_TEXT = "interaction.DashboardDisplay.VisualText";
	public static final String INT_DASH_ICON = "interaction.DashboardDisplay.VisualIcon";
	public static final String INT_SPEAKERS_SOUND = "interaction.Speakers.AuditorySound";
	public static final String INT_SPEAKERS_BEEP = "interaction.Speakers.AuditoryBeep";

	// Interfaces provided/required (constantes habituales en los ARC de Tatami)
	public static final String PROVIDED_DEVICE = "provided_device";
	public static final String PROVIDED_SENSOR = "provided_sensor";
	public static final String PROVIDED_SERVICE = "provided_service";
	public static final String PROVIDED_DRIVINGSERVICE = "provided_drivingservice";
	public static final String PROVIDED_MECHANISM = "provided_mechanism";

	public static final String REQ_ENGINE = "required_engine";
	public static final String REQ_STEERING = "required_steering";
	public static final String REQ_FRONT_DIST = "required_frontdistancesensor";
	public static final String REQ_REAR_DIST = "required_reardistancesensor";
	public static final String REQ_LEFT_DIST = "required_leftdistancesensor";
	public static final String REQ_RIGHT_DIST = "required_rightdistancesensor";
	public static final String REQ_LEFT_LINE = "required_leftlinesensor";
	public static final String REQ_RIGHT_LINE = "required_rightlinesensor";
	public static final String REQ_HUMANSENSORS = "required_humansensors";
	public static final String REQ_ROADSENSOR = "required_roadsensor";
	public static final String REQ_FALLBACKPLAN = "required_fallbackplan";
	public static final String REQ_NOTIFICATION = "required_notificationservice";
	public static final String REQ_MECHANISMS = "required_mechanisms";

	/**
	 * Quita cualquier servicio de conducción L3 que pudiera estar activo.
	 */
	public static void removeAnyL3(IRuleComponentsSystemConfiguration cfg) {
		SystemConfigurationHelper.componentToRemove(cfg, DRV_L3_HIGHWAY, V);
		SystemConfigurationHelper.componentToRemove(cfg, DRV_L3_TRAFFICJAM, V);
		SystemConfigurationHelper.componentToRemove(cfg, DRV_L3_CITY, V);
	}

	/**
	 * Quita cualquier servicio L2 activo.
	 */
	public static void removeAnyL2(IRuleComponentsSystemConfiguration cfg) {
		SystemConfigurationHelper.componentToRemove(cfg, DRV_L2_ACC, V);
		SystemConfigurationHelper.componentToRemove(cfg, DRV_L2_LKA, V);
	}

	/**
	 * Quita el servicio L1 si estaba activo.
	 */
	public static void removeL1(IRuleComponentsSystemConfiguration cfg) {
		SystemConfigurationHelper.componentToRemove(cfg, DRV_L1_ASSISTED, V);
	}

	/**
	 * Añade los bindings necesarios para que un servicio L3 (cualquier variante)
	 * tenga todas sus dependencias cableadas: motor, dirección, sensores de
	 * distancia, sensores de línea, RoadSensor, HumanSensors, NotificationService
	 * y FallbackPlan.
	 */
	public static void wireL3(IRuleComponentsSystemConfiguration cfg, String l3Id) {
		// Motor y dirección (L2)
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_ENGINE, DEV_ENGINE, V, PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_STEERING, DEV_STEERING, V, PROVIDED_DEVICE);

		// Distancia (L2)
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_FRONT_DIST, DEV_FRONT_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_REAR_DIST, DEV_REAR_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_LEFT_DIST, DEV_LEFT_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_RIGHT_DIST, DEV_RIGHT_DIST, V, PROVIDED_SENSOR);

		// Líneas (L1)
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_LEFT_LINE, DEV_LEFT_LINE, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_RIGHT_LINE, DEV_RIGHT_LINE, V, PROVIDED_SENSOR);

		// Específico L3
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_HUMANSENSORS, DEV_HUMANSENSORS, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_ROADSENSOR, DEV_ROADSENSOR, V, PROVIDED_SENSOR);

		// Notificaciones
		SystemConfigurationHelper.bindingToAdd(cfg, l3Id, V, REQ_NOTIFICATION, INT_NOTIFICATION, V, PROVIDED_SERVICE);
	}

	/**
	 * Cableado básico para L2_AdaptiveCruiseControl / L2_LaneKeepingAssist.
	 */
	public static void wireL2(IRuleComponentsSystemConfiguration cfg, String l2Id) {
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_ENGINE, DEV_ENGINE, V, PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_STEERING, DEV_STEERING, V, PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_FRONT_DIST, DEV_FRONT_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_REAR_DIST, DEV_REAR_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_LEFT_DIST, DEV_LEFT_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_RIGHT_DIST, DEV_RIGHT_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_LEFT_LINE, DEV_LEFT_LINE, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_RIGHT_LINE, DEV_RIGHT_LINE, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l2Id, V, REQ_NOTIFICATION, INT_NOTIFICATION, V, PROVIDED_SERVICE);
	}

	/**
	 * Cableado básico para L1_AssistedDriving.
	 */
	public static void wireL1(IRuleComponentsSystemConfiguration cfg) {
		SystemConfigurationHelper.bindingToAdd(cfg, DRV_L1_ASSISTED, V, REQ_FRONT_DIST, DEV_FRONT_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, DRV_L1_ASSISTED, V, REQ_LEFT_LINE, DEV_LEFT_LINE, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, DRV_L1_ASSISTED, V, REQ_RIGHT_LINE, DEV_RIGHT_LINE, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, DRV_L1_ASSISTED, V, REQ_NOTIFICATION, INT_NOTIFICATION, V, PROVIDED_SERVICE);
	}

	/**
	 * Cablea un fallback plan de emergencia (sólo motor y dirección).
	 */
	public static void wireFallbackEmergency(IRuleComponentsSystemConfiguration cfg) {
		SystemConfigurationHelper.bindingToAdd(cfg, FB_EMERGENCY, V, REQ_ENGINE, DEV_ENGINE, V, PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(cfg, FB_EMERGENCY, V, REQ_STEERING, DEV_STEERING, V, PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(cfg, FB_EMERGENCY, V, REQ_NOTIFICATION, INT_NOTIFICATION, V, PROVIDED_SERVICE);
	}

	/**
	 * Cablea el fallback plan de aparcar en cuneta (motor, dirección, sensor de
	 * distancia derecho y sensor de línea derecho).
	 */
	public static void wireFallbackPark(IRuleComponentsSystemConfiguration cfg) {
		SystemConfigurationHelper.bindingToAdd(cfg, FB_PARK, V, REQ_ENGINE, DEV_ENGINE, V, PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(cfg, FB_PARK, V, REQ_STEERING, DEV_STEERING, V, PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(cfg, FB_PARK, V, REQ_RIGHT_DIST, DEV_RIGHT_DIST, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, FB_PARK, V, REQ_RIGHT_LINE, DEV_RIGHT_LINE, V, PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, FB_PARK, V, REQ_NOTIFICATION, INT_NOTIFICATION, V, PROVIDED_SERVICE);
	}

	/**
	 * Conecta un mecanismo de interacción al NotificationService (binding al
	 * puerto multi-cardinalidad "required_mechanisms").
	 */
	public static void wireInteractionMechanism(IRuleComponentsSystemConfiguration cfg, String mechanismId) {
		SystemConfigurationHelper.bindingToAdd(cfg, INT_NOTIFICATION, V, REQ_MECHANISMS, mechanismId, V, PROVIDED_MECHANISM);
	}

	/**
	 * Desconecta un mecanismo de interacción del NotificationService.
	 */
	public static void unwireInteractionMechanism(IRuleComponentsSystemConfiguration cfg, String mechanismId) {
		SystemConfigurationHelper.bindingToRemove(cfg, INT_NOTIFICATION, V, REQ_MECHANISMS, mechanismId, V, PROVIDED_MECHANISM);
	}

}
