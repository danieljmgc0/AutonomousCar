package autonomouscar.mapek.lite.adaptation.resources;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import sua.autonomouscar.infraestructure.devices.ARC.DistanceSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.EngineARC;
import sua.autonomouscar.infraestructure.devices.ARC.HumanSensorsARC;
import sua.autonomouscar.infraestructure.devices.ARC.LineSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.RoadSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.SteeringARC;
import sua.autonomouscar.infraestructure.driving.ARC.DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.FallbackPlanARC;
import sua.autonomouscar.infraestructure.driving.ARC.L1_DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.L2_DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.L3_DrivingServiceARC;
import sua.autonomouscar.infraestructure.interaction.ARC.AuditoryBeepARC;
import sua.autonomouscar.infraestructure.interaction.ARC.HapticVibrationARC;
import sua.autonomouscar.infraestructure.interaction.ARC.NotificationServiceARC;

public class ConfiguracionHelper {

	// -------------------------------------------------------------------------
	// Añadir/quitar servicios de conducción L3
	// -------------------------------------------------------------------------

	public static void addL3HighwayChauffer(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToAdd(c, "driving.L3.HighwayChauffer", "1.0.0");
		bindL3(c, "driving.L3.HighwayChauffer");
	}

	public static void addL3CityChauffer(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToAdd(c, "driving.L3.CityChauffer", "1.0.0");
		bindL3(c, "driving.L3.CityChauffer");
	}

	public static void addL3TrafficJamChauffer(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToAdd(c, "driving.L3.TrafficJamChauffer", "1.0.0");
		bindL3(c, "driving.L3.TrafficJamChauffer");
	}

	public static void removeL3HighwayChauffer(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToRemove(c, "driving.L3.HighwayChauffer", "1.0.0");
	}

	public static void removeL3CityChauffer(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToRemove(c, "driving.L3.CityChauffer", "1.0.0");
	}

	public static void removeL3TrafficJamChauffer(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToRemove(c, "driving.L3.TrafficJamChauffer", "1.0.0");
	}

	public static void removeAllL3(IRuleComponentsSystemConfiguration c) {
		removeL3HighwayChauffer(c);
		removeL3CityChauffer(c);
		removeL3TrafficJamChauffer(c);
	}

	// -------------------------------------------------------------------------
	// Añadir/quitar servicios L2 y L1
	// -------------------------------------------------------------------------

	public static void addL2AdaptiveCruiseControl(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToAdd(c, "driving.L2.AdaptiveCruiseControl", "1.0.0");
		bindL2(c, "driving.L2.AdaptiveCruiseControl");
	}

	public static void addL1AssistedDriving(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToAdd(c, "driving.L1.AssistedDriving", "1.0.0");
		bindL1(c, "driving.L1.AssistedDriving");
	}

	public static void removeL2AdaptiveCruiseControl(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToRemove(c, "driving.L2.AdaptiveCruiseControl", "1.0.0");
	}

	public static void removeL1AssistedDriving(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToRemove(c, "driving.L1.AssistedDriving", "1.0.0");
	}

	// -------------------------------------------------------------------------
	// Bindings por nivel
	// -------------------------------------------------------------------------

	public static void bindL1(IRuleComponentsSystemConfiguration c, String ds) {
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L1_DrivingServiceARC.REQUIRED_FRONTDISTANCESENSOR,
				"device.FrontDistanceSensor", "1.0.0", DistanceSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L1_DrivingServiceARC.REQUIRED_RIGHTLINESENSOR,
				"device.RightLineSensor", "1.0.0", LineSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L1_DrivingServiceARC.REQUIRED_LEFTLINESENSOR,
				"device.LeftLineSensor", "1.0.0", LineSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L1_DrivingServiceARC.REQUIRED_NOTIFICATIONSERVICE,
				"interaction.NotificationService", "1.0.0", NotificationServiceARC.PROVIDED_SERVICE);
	}

	public static void bindL2(IRuleComponentsSystemConfiguration c, String ds) {
		bindL1(c, ds);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L2_DrivingServiceARC.REQUIRED_ENGINE,
				"device.Engine", "1.0.0", EngineARC.PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L2_DrivingServiceARC.REQUIRED_STEERING,
				"device.Steering", "1.0.0", SteeringARC.PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L2_DrivingServiceARC.REQUIRED_REARDISTANCESENSOR,
				"device.RearDistanceSensor", "1.0.0", DistanceSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L2_DrivingServiceARC.REQUIRED_RIGHTDISTANCESENSOR,
				"device.RightDistanceSensor", "1.0.0", DistanceSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L2_DrivingServiceARC.REQUIRED_LEFTDISTANCESENSOR,
				"device.LeftDistanceSensor", "1.0.0", DistanceSensorARC.PROVIDED_SENSOR);
	}

	public static void bindL3(IRuleComponentsSystemConfiguration c, String ds) {
		bindL2(c, ds);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L3_DrivingServiceARC.REQUIRED_HUMANSENSORS,
				"device.HumanSensors", "1.0.0", HumanSensorsARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L3_DrivingServiceARC.REQUIRED_ROADSENSOR,
				"device.RoadSensor", "1.0.0", RoadSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c, ds, "1.0.0", L3_DrivingServiceARC.REQUIRED_FALLBACKPLAN,
				"driving.FallbackPlan.Emergency", "1.0.0", DrivingServiceARC.PROVIDED_DRIVINGSERVICE);
	}

	// -------------------------------------------------------------------------
	// Bindings de FallbackPlans
	// -------------------------------------------------------------------------

	public static void addFallbackPlanEmergency(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToAdd(c, "driving.FallbackPlan.Emergency", "1.0.0");
		SystemConfigurationHelper.bindingToAdd(c,
				"driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_ENGINE,
				"device.Engine", "1.0.0", EngineARC.PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(c,
				"driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_STEERING,
				"device.Steering", "1.0.0", SteeringARC.PROVIDED_DEVICE);
	}

	public static void addFallbackPlanParkInShoulder(IRuleComponentsSystemConfiguration c) {
		SystemConfigurationHelper.componentToAdd(c, "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0");
		SystemConfigurationHelper.bindingToAdd(c,
				"driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", FallbackPlanARC.REQUIRED_ENGINE,
				"device.Engine", "1.0.0", EngineARC.PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(c,
				"driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", FallbackPlanARC.REQUIRED_STEERING,
				"device.Steering", "1.0.0", SteeringARC.PROVIDED_DEVICE);
		SystemConfigurationHelper.bindingToAdd(c,
				"driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", L2_DrivingServiceARC.REQUIRED_RIGHTDISTANCESENSOR,
				"device.RightDistanceSensor", "1.0.0", DistanceSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c,
				"driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", L1_DrivingServiceARC.REQUIRED_RIGHTLINESENSOR,
				"device.RightLineSensor", "1.0.0", LineSensorARC.PROVIDED_SENSOR);
	}

	// -------------------------------------------------------------------------
	// Bindings de NotificationService con mecanismos de interacción
	// -------------------------------------------------------------------------

	public static void bindNotifToMechanism(IRuleComponentsSystemConfiguration c, String mechanismBundle) {
		SystemConfigurationHelper.bindingToAdd(c,
				"interaction.NotificationService", "1.0.0", NotificationServiceARC.REQUIRED_SERVICE,
				mechanismBundle, "1.0.0", HapticVibrationARC.PROVIDED_MECHANISM);
	}

	public static void unbindNotifFromMechanism(IRuleComponentsSystemConfiguration c, String mechanismBundle) {
		SystemConfigurationHelper.bindingToRemove(c,
				"interaction.NotificationService", "1.0.0", NotificationServiceARC.REQUIRED_SERVICE,
				mechanismBundle, "1.0.0", AuditoryBeepARC.PROVIDED_MECHANISM);
	}

	// Mecanismos disponibles
	public static final String MECH_STEERING_WHEEL   = "interaction.SteeringWheel";
	public static final String MECH_SEAT_DRIVER      = "interaction.Seat.Driver";
	public static final String MECH_SEAT_COPILOT     = "interaction.Seat.Copilot";
	public static final String MECH_SPEAKER_BEEP     = "interaction.Speakers.AuditoryBeep";
	public static final String MECH_SPEAKER_SOUND    = "interaction.Speakers.AuditorySound";
	public static final String MECH_DRIVER_ICON      = "interaction.DriverDisplay.VisualIcon";
	public static final String MECH_DRIVER_TEXT      = "interaction.DriverDisplay.VisualText";
	public static final String MECH_DASHBOARD_ICON   = "interaction.DashboardDisplay.VisualIcon";
	public static final String MECH_DASHBOARD_TEXT   = "interaction.DashboardDisplay.VisualText";
	public static final String MECH_DASHBOARD_ICON2  = "interaction.DashboardIcon";

	public static void addMechanism(IRuleComponentsSystemConfiguration c, String bundle) {
		SystemConfigurationHelper.componentToAdd(c, bundle, "1.0.0");
		bindNotifToMechanism(c, bundle);
	}

	public static void removeMechanism(IRuleComponentsSystemConfiguration c, String bundle) {
		unbindNotifFromMechanism(c, bundle);
		SystemConfigurationHelper.componentToRemove(c, bundle, "1.0.0");
	}

	public static void removeAllMechanisms(IRuleComponentsSystemConfiguration c) {
		removeMechanism(c, MECH_STEERING_WHEEL);
		removeMechanism(c, MECH_SEAT_DRIVER);
		removeMechanism(c, MECH_SEAT_COPILOT);
		removeMechanism(c, MECH_SPEAKER_BEEP);
		removeMechanism(c, MECH_SPEAKER_SOUND);
		removeMechanism(c, MECH_DRIVER_ICON);
		removeMechanism(c, MECH_DRIVER_TEXT);
		removeMechanism(c, MECH_DASHBOARD_ICON);
		removeMechanism(c, MECH_DASHBOARD_TEXT);
		removeMechanism(c, MECH_DASHBOARD_ICON2);
	}
}
