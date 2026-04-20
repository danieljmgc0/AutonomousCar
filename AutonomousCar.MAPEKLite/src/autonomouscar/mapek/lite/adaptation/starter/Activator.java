package autonomouscar.mapek.lite.adaptation.starter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomouscar.mapek.lite.adaptation.resources.IluminacionConfortAdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorModo;
import autonomouscar.mapek.lite.adaptation.resources.SondaModo;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;
import sua.autonomouscar.infraestructure.devices.ARC.EngineARC;
import sua.autonomouscar.infraestructure.devices.ARC.SteeringARC;
import sua.autonomouscar.infraestructure.driving.ARC.FallbackPlanARC;
import sua.autonomouscar.infraestructure.driving.ARC.L3_DrivingServiceARC;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		BasicMAPEKLiteLoopHelper.BUNDLECONTEXT = bundleContext;
		BasicMAPEKLiteLoopHelper.REFERENCE_MODEL = "AutonomousCar"; //System.getProperty("model", "default-model");

		// ... adding the initial system configuration
		IComponentsSystemConfiguration theInitialSystemConfiguration = 
				SystemConfigurationHelper.createSystemConfiguration("InititalConfiguration");
		SystemConfigurationHelper.addComponent(theInitialSystemConfiguration, "device.RoadSensor", "1.0.0");
		BasicMAPEKLiteLoopHelper.INITIAL_SYSTEMCONFIGURATION = theInitialSystemConfiguration;

		BasicMAPEKLiteLoopHelper.MODELSREPOSITORY_FOLDER = System.getProperty("modelsrepository.folder");
		BasicMAPEKLiteLoopHelper.ADAPTATIONREPORTS_FOLDER = System.getProperty("adaptationreports.folder");
		// STARTING THE MAPE-K LOOP
		
		BasicMAPEKLiteLoopHelper.startLoopModules();

		
		
		BasicMAPEKLiteLoopHelper.addInitialSelfConfigurationCapabilities(createInitialSystemConfiguration());
		
		
		
		// ADAPTATION PROPERTIES
		IKnowledgeProperty kp_Modo = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("Modo");

		// ADAPTATION RULES
 		IAdaptiveReadyComponent theIluminacionConfortAdaptationRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new IluminacionConfortAdaptationRule(bundleContext));		
 		
		// MONITORS
		IAdaptiveReadyComponent theModoMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorModo(bundleContext));		

		// PROBES
		IAdaptiveReadyComponent theModoProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new SondaModo(bundleContext), theModoMonitorARC);

		//
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	
	protected IRuleComponentsSystemConfiguration createInitialSystemConfiguration() {
		
		IRuleComponentsSystemConfiguration theInitialSystemConfiguration = SystemConfigurationHelper.createPartialSystemConfiguration("InitialConfiguration_" + ITimeStamped.getCurrentTimeStamp());
			
		//
		// ... adding and removing components examples ...
		// SystemConfigurationHelper.componentToAdd or SystemConfigurationHelper.componentToRemove
		//		systemconfiguration :  una IRuleComponentsSystemConfiguration donde se añadirán o eliminarán los componentes
		//		component-id		:  nombre del compopnente a añadir o quitar
		//		component-version	:  versión del componente
		
		// Ejemplo 1: Añadimos los componentes "device.RoadSensor" y "device.Engine", y eliminamos el componente "device.Steering" ...
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RoadSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.Engine", "1.0.0");
		SystemConfigurationHelper.componentToRemove(theInitialSystemConfiguration, "device.Steering", "1.0.0");
		
		// Ejemplo 2: ... y añadimos el servicio "driving.FallbackPlan.Emergency", que representa al fallback plan de emergencia
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "driving.FallbackPlan.Emergency", "1.0.0");
		
		
		
		//
		// ... adding and removing binding examples ...
		// SystemConfigurationHelper.bindingToAdd or SystemConfigurationHelper.bindingToRemove
		//		systemconfiguration   :  una IRuleComponentsSystemConfiguration donde se añadirán o eliminarán los componentes
		//		req-component-id	  :  nombre del componente que requiere la conexión
		//		req-component-version :  versión del componente que requiere la conexión
		//		req-component-interfaz:  interfaz requerida del componente
		//      prov-component-id	  :  nombre del componente que provee la conexión
		//		prov-component-version:  versión del componente que provee la conexión
		//		prov-component-interfaz:  interfaz proporcionada del componente
		
		// Ejemplo 3: Conectar el componente "driving.FallbackPlan.Emergency" (a través de su interfaz requerida "required_engine")
		//    con el componente "device.Engine" (a través de su interfaz proporcionada "provided_device")
		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration, 
				"driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_ENGINE,
				"device.Engine", "1.0.0", EngineARC.PROVIDED_DEVICE);

		// Ejemplo 4: Desconectar del componente "driving.FallbackPlan.Emergency" (en su interfaz requerida "required_steering")
		//    del componente "device.Steering" (a través de su interfaz proporcionada "provided_device")
		SystemConfigurationHelper.bindingToRemove(theInitialSystemConfiguration, 
				"driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_STEERING,
				"device.Steering", "1.0.0", SteeringARC.PROVIDED_DEVICE);

		
		//
		// ... setting parameters examples ...
		// SystemConfigurationHelper.setParameter
		//		systemconfiguration   :  una IRuleComponentsSystemConfiguration donde se añadirán el set parameter
		//		component-id		  :  nombre del componente
		//		component-version	  :  versión del componente
		//		parameter-id		  :  nombre del parámetro
		//		parameter-value		  :  valor del parámetro
		
		// Ejemplo 5: Establecer el parámetro "referencespeed" a 100Km/h del servicio de conducción "driving.L3.HighwayChauffer"
		SystemConfigurationHelper.setParameter(theInitialSystemConfiguration, 
				"driving.L3.HighwayChauffer", "1.0.0", L3_DrivingServiceARC.PARAMETER_REFERENCESPEED, "100");
		// * El servicio de conducción "driving.L3.HighwayChauffer" puede no estar activo en este momento, y por tanto
		//   este 'set parameter' puede que no provoque ningún cambio de manera efectiva.
		//   Si quisiéramos que el servicio "driving.L3.HighwayChauffer" estuviera activo, deberíamos añadirlo como en el Ejemplo 2
		// ...

		return theInitialSystemConfiguration;
		
	}
}
