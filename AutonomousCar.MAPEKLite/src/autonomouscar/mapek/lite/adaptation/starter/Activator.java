package autonomouscar.mapek.lite.adaptation.starter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomouscar.mapek.lite.adaptation.resources.MonitorAsientoConductor;
import autonomouscar.mapek.lite.adaptation.resources.MonitorAsientoCopiloto;
import autonomouscar.mapek.lite.adaptation.resources.MonitorAtencionConductor;
import autonomouscar.mapek.lite.adaptation.resources.MonitorDeteccionManosVolante;
import autonomouscar.mapek.lite.adaptation.resources.MonitorFalloCriticoSistema;
import autonomouscar.mapek.lite.adaptation.resources.MonitorModoConduccion;
import autonomouscar.mapek.lite.adaptation.resources.MonitorPosibilidadConduccion;
import autonomouscar.mapek.lite.adaptation.resources.MonitorTipoVia;
import autonomouscar.mapek.lite.adaptation.resources.MonitorTraficoVia;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarEstadoViaAutopista;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarEstadoViaAutovia;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarEstadoViaCiudad;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarFallbackPlan;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarSensorEnFallo;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarTipoViaAtasco;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarTipoViaCiudad;
import autonomouscar.mapek.lite.adaptation.resources.ReglaCambioAtencionConductor;
import autonomouscar.mapek.lite.adaptation.resources.ReglaCambioManosVolante;
import autonomouscar.mapek.lite.adaptation.resources.ReglaCambioUbicacionConductor;
import autonomouscar.mapek.lite.adaptation.resources.ReglaEnCarreteraEstandar;
import autonomouscar.mapek.lite.adaptation.resources.ReglaFalloCritico;
import autonomouscar.mapek.lite.adaptation.resources.SondaAsientoConductor;
import autonomouscar.mapek.lite.adaptation.resources.SondaAsientoCopiloto;
import autonomouscar.mapek.lite.adaptation.resources.SondaAtencionConductor;
import autonomouscar.mapek.lite.adaptation.resources.SondaDeteccionManosVolante;
import autonomouscar.mapek.lite.adaptation.resources.SondaFalloCriticoSistema;
import autonomouscar.mapek.lite.adaptation.resources.SondaModoConduccion;
import autonomouscar.mapek.lite.adaptation.resources.SondaPosibilidadConduccion;
import autonomouscar.mapek.lite.adaptation.resources.SondaTipoVia;
import autonomouscar.mapek.lite.adaptation.resources.SondaTraficoVia;
import autonomouscar.mapek.lite.adaptation.resources.ConfiguracionHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.resources.ARC.artifacts.components.arc.ProbeARC;
import es.upv.pros.tatami.adaptation.mapek.lite.structures.systemconfiguration.interfaces.IRuleSystemConfiguration;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;
import sua.autonomouscar.infraestructure.devices.ARC.DistanceSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.DriverFaceMonitorARC;
import sua.autonomouscar.infraestructure.devices.ARC.EngineARC;
import sua.autonomouscar.infraestructure.devices.ARC.HandsOnWheelSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.HumanSensorsARC;
import sua.autonomouscar.infraestructure.devices.ARC.LineSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.RoadSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.SeatSensorARC;
import sua.autonomouscar.infraestructure.devices.ARC.SteeringARC;
import sua.autonomouscar.infraestructure.driving.ARC.DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.FallbackPlanARC;
import sua.autonomouscar.infraestructure.driving.ARC.L1_DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.L2_DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.L3_DrivingServiceARC;
import sua.autonomouscar.infraestructure.interaction.ARC.AuditoryBeepARC;
import sua.autonomouscar.infraestructure.interaction.ARC.HapticVibrationARC;
import sua.autonomouscar.infraestructure.interaction.ARC.NotificationServiceARC;
import sua.autonomouscar.simulation.interfaces.ISimulationElement;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		BasicMAPEKLiteLoopHelper.BUNDLECONTEXT = bundleContext;
		BasicMAPEKLiteLoopHelper.REFERENCE_MODEL = "AutonomousCar";

		// Configuración inicial: solo L0_ManualDriving y RoadSensor (siempre activo)
		IComponentsSystemConfiguration initialConfig =
				SystemConfigurationHelper.createSystemConfiguration("InitialConfiguration");
		SystemConfigurationHelper.addComponent(initialConfig, "driving.L0.ManualDriving", "1.0.0");
		SystemConfigurationHelper.addComponent(initialConfig, "device.RoadSensor", "1.0.0");
		BasicMAPEKLiteLoopHelper.INITIAL_SYSTEMCONFIGURATION = initialConfig;

		BasicMAPEKLiteLoopHelper.MODELSREPOSITORY_FOLDER = System.getProperty("modelsrepository.folder");
		BasicMAPEKLiteLoopHelper.ADAPTATIONREPORTS_FOLDER = System.getProperty("adaptationreports.folder");

		BasicMAPEKLiteLoopHelper.startLoopModules();
		BasicMAPEKLiteLoopHelper.addInitialSelfConfigurationCapabilities(createInitialSystemConfiguration());

		// ---- KNOWLEDGE PROPERTIES ----
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("nivel-autonomia");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("modo-conduccion");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("tipo-via");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("trafico-via");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("atencion-conductor");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("asiento-conductor-ocupado");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("asiento-copiloto-ocupado");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("deteccion-manos-volante");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("posibilidad-conduccion");
		BasicMAPEKLiteLoopHelper.createKnowledgeProperty("fallo-critico-sistema");

		// ---- REGLAS ----
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaEnCarreteraEstandar(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarTipoViaAtasco(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarTipoViaCiudad(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarEstadoViaAutopista(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarEstadoViaCiudad(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarEstadoViaAutovia(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarSensorEnFallo(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarFallbackPlan(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaFalloCritico(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaCambioAtencionConductor(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaCambioManosVolante(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaCambioUbicacionConductor(bundleContext));

		// ---- MONITORES Y SONDAS ----
		deployProbeMonitor(bundleContext, new SondaTipoVia(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorTipoVia(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaTraficoVia(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorTraficoVia(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaModoConduccion(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorModoConduccion(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaAtencionConductor(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorAtencionConductor(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaAsientoConductor(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorAsientoConductor(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaAsientoCopiloto(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorAsientoCopiloto(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaDeteccionManosVolante(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorDeteccionManosVolante(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaPosibilidadConduccion(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorPosibilidadConduccion(bundleContext)));

		deployProbeMonitor(bundleContext, new SondaFalloCriticoSistema(bundleContext),
				BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorFalloCriticoSistema(bundleContext)));
	}

	/** Despliega la sonda y la registra como ISimulationElement para que sea invocada en cada 'next'. */
	private void deployProbeMonitor(BundleContext ctx, ISimulationElement sonda, IAdaptiveReadyComponent monitorARC) {
		BasicMAPEKLiteLoopHelper.deployProbe((es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe) sonda, monitorARC);
		ctx.registerService(ISimulationElement.class.getName(), sonda, null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	/**
	 * Configuración inicial que se aplica al ejecutar 'initialize' en la consola OSGi.
	 * Despliega el escenario de prueba: vehículo en L3_HighwayChauffer en autopista.
	 */
	protected IRuleSystemConfiguration createInitialSystemConfiguration() {
		IRuleComponentsSystemConfiguration c = SystemConfigurationHelper
				.createPartialSystemConfiguration("InitialScenario_" + ITimeStamped.getCurrentTimeStamp());

		// --- Sensores de distancia ---
		SystemConfigurationHelper.componentToAdd(c, "device.Engine", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.Steering", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.FrontDistanceSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.RearDistanceSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.RightDistanceSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.LeftDistanceSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.RightLineSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.LeftLineSensor", "1.0.0");

		// --- Sensores humanos (HumanSensors + sub-sensores) ---
		SystemConfigurationHelper.componentToAdd(c, "device.DriverFaceMonitor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.DriverSeatSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.CopilotSeatSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.HandsOnWheelSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(c, "device.HumanSensors", "1.0.0");

		// Bindings internos de HumanSensors
		SystemConfigurationHelper.bindingToAdd(c,
				"device.HumanSensors", "1.0.0", HumanSensorsARC.REQUIRED_FACEMONITOR,
				"device.DriverFaceMonitor", "1.0.0", DriverFaceMonitorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c,
				"device.HumanSensors", "1.0.0", HumanSensorsARC.REQUIRED_DRIVERSEATSENSOR,
				"device.DriverSeatSensor", "1.0.0", SeatSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c,
				"device.HumanSensors", "1.0.0", HumanSensorsARC.REQUIRED_COPILOTSEATSENSOR,
				"device.CopilotSeatSensor", "1.0.0", SeatSensorARC.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(c,
				"device.HumanSensors", "1.0.0", HumanSensorsARC.REQUIRED_HANDSONWHEELSENSOR,
				"device.HandsOnWheelSensor", "1.0.0", HandsOnWheelSensorARC.PROVIDED_SENSOR);

		// --- Servicio de notificación ---
		SystemConfigurationHelper.componentToAdd(c, "interaction.NotificationService", "1.0.0");

		// --- Fallback Plans ---
		ConfiguracionHelper.addFallbackPlanEmergency(c);
		ConfiguracionHelper.addFallbackPlanParkInShoulder(c);

		// --- Servicio de conducción L3_HighwayChauffer ---
		ConfiguracionHelper.addL3HighwayChauffer(c);

		// Sobreescribir el fallback plan del L3 con el preferente (ParkInShoulder)
		SystemConfigurationHelper.bindingToAdd(c,
				"driving.L3.HighwayChauffer", "1.0.0", L3_DrivingServiceARC.REQUIRED_FALLBACKPLAN,
				"driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", DrivingServiceARC.PROVIDED_DRIVINGSERVICE);

		return c;
	}
}
