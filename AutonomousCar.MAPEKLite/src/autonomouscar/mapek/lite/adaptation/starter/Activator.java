package autonomouscar.mapek.lite.adaptation.starter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomouscar.mapek.lite.adaptation.resources.ConfiguracionHelper;
import autonomouscar.mapek.lite.adaptation.resources.MonitorAsientoConductorOcupado;
import autonomouscar.mapek.lite.adaptation.resources.MonitorAsientoCopilotoOcupado;
import autonomouscar.mapek.lite.adaptation.resources.MonitorAtencionConductor;
import autonomouscar.mapek.lite.adaptation.resources.MonitorDeteccionManosVolante;
import autonomouscar.mapek.lite.adaptation.resources.MonitorFalloCriticoSistema;
import autonomouscar.mapek.lite.adaptation.resources.MonitorModoConduccion;
import autonomouscar.mapek.lite.adaptation.resources.MonitorNivelAutonomia;
import autonomouscar.mapek.lite.adaptation.resources.MonitorTipoVia;
import autonomouscar.mapek.lite.adaptation.resources.MonitorTraficoVia;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarEstadoViaAutopista;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarEstadoViaAutovia;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarEstadoViaCiudad;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarFallbackPlan;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarSensorEnFallo;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarTipoViaAtasco;
import autonomouscar.mapek.lite.adaptation.resources.ReglaActivarTipoViaCiudad;
import autonomouscar.mapek.lite.adaptation.resources.ReglaArranqueHighwayChauffer;
import autonomouscar.mapek.lite.adaptation.resources.ReglaAtencionConductor;
import autonomouscar.mapek.lite.adaptation.resources.ReglaAutoCuracionFalloCritico;
import autonomouscar.mapek.lite.adaptation.resources.ReglaEnCarreteraEstandar;
import autonomouscar.mapek.lite.adaptation.resources.ReglaManosVolante;
import autonomouscar.mapek.lite.adaptation.resources.ReglaPriorizacionSensores;
import autonomouscar.mapek.lite.adaptation.resources.ReglaUbicacionConductor;
import autonomouscar.mapek.lite.adaptation.resources.SondaAsientoConductorOcupado;
import autonomouscar.mapek.lite.adaptation.resources.SondaAsientoCopilotoOcupado;
import autonomouscar.mapek.lite.adaptation.resources.SondaAtencionConductor;
import autonomouscar.mapek.lite.adaptation.resources.SondaDeteccionManosVolante;
import autonomouscar.mapek.lite.adaptation.resources.SondaFalloCriticoSistema;
import autonomouscar.mapek.lite.adaptation.resources.SondaModoConduccion;
import autonomouscar.mapek.lite.adaptation.resources.SondaNivelAutonomia;
import autonomouscar.mapek.lite.adaptation.resources.SondaTipoVia;
import autonomouscar.mapek.lite.adaptation.resources.SondaTraficoVia;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private SondaModoConduccion sondaModoConduccion;
	private SondaTipoVia sondaTipoVia;
	private SondaTraficoVia sondaTraficoVia;
	private SondaAtencionConductor sondaAtencionConductor;
	private SondaAsientoConductorOcupado sondaAsientoConductor;
	private SondaAsientoCopilotoOcupado sondaAsientoCopiloto;
	private SondaNivelAutonomia sondaNivelAutonomia;
	private SondaDeteccionManosVolante sondaManosVolante;
	private SondaFalloCriticoSistema sondaFalloCritico;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		BasicMAPEKLiteLoopHelper.BUNDLECONTEXT = bundleContext;
		BasicMAPEKLiteLoopHelper.REFERENCE_MODEL = "AutonomousCar";

		// Configuración inicial del sistema gestionado: sólo L0_ManualDriving
		IComponentsSystemConfiguration initial = SystemConfigurationHelper
				.createSystemConfiguration("InitialConfiguration");
		SystemConfigurationHelper.addComponent(initial, ConfiguracionHelper.DRV_L0_MANUAL, ConfiguracionHelper.V);
		BasicMAPEKLiteLoopHelper.INITIAL_SYSTEMCONFIGURATION = initial;

		BasicMAPEKLiteLoopHelper.MODELSREPOSITORY_FOLDER = System.getProperty("modelsrepository.folder");
		BasicMAPEKLiteLoopHelper.ADAPTATIONREPORTS_FOLDER = System.getProperty("adaptationreports.folder");

		// Arrancamos el bucle MAPE-K
		BasicMAPEKLiteLoopHelper.startLoopModules();

		// Auto-configuración inicial: dejar L0_ManualDriving como única
		// configuración tras el arranque del bucle.
		BasicMAPEKLiteLoopHelper.addInitialSelfConfigurationCapabilities(createInitialSelfConfiguration());

		// ===== KNOWLEDGE PROPERTIES =====
		// Todas las KPs se crean CON VALOR INICIAL. Si una KP queda con
		// getValue()==null, getKnowledgeProperty(...) en otras consultas
		// puede bloquearse en el módulo Knowledge de Tatami.
		setIfPresent(getOrCreateKP("modo-conduccion"), "L0_M");
		setIfPresent(getOrCreateKP("nivel-autonomia"), Integer.valueOf(0));
		setIfPresent(getOrCreateKP("tipo-via"), "Carretera");
		setIfPresent(getOrCreateKP("trafico-via"), "Fluido");
		setIfPresent(getOrCreateKP("atencion-conductor"), "Atento");
		setIfPresent(getOrCreateKP("asiento-conductor-ocupado"), Boolean.FALSE);
		setIfPresent(getOrCreateKP("asiento-copiloto-ocupado"), Boolean.FALSE);
		setIfPresent(getOrCreateKP("deteccion-manos-volante"), Boolean.FALSE);
		setIfPresent(getOrCreateKP("fallo-critico-sistema"), Boolean.FALSE);

		// ===== MONITORES =====
		IAdaptiveReadyComponent arcMonitorModo = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorModoConduccion(bundleContext));
		IAdaptiveReadyComponent arcMonitorTipoVia = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorTipoVia(bundleContext));
		IAdaptiveReadyComponent arcMonitorTrafico = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorTraficoVia(bundleContext));
		IAdaptiveReadyComponent arcMonitorAtencion = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorAtencionConductor(bundleContext));
		IAdaptiveReadyComponent arcMonitorAsientoCond = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorAsientoConductorOcupado(bundleContext));
		IAdaptiveReadyComponent arcMonitorAsientoCop = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorAsientoCopilotoOcupado(bundleContext));
		IAdaptiveReadyComponent arcMonitorNivelAutonomia = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorNivelAutonomia(bundleContext));
		IAdaptiveReadyComponent arcMonitorManos = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorDeteccionManosVolante(bundleContext));
		IAdaptiveReadyComponent arcMonitorFalloCritico = BasicMAPEKLiteLoopHelper
				.deployMonitor(new MonitorFalloCriticoSistema(bundleContext));

		// ===== SONDAS =====
		this.sondaModoConduccion = new SondaModoConduccion(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaModoConduccion, arcMonitorModo);

		this.sondaTipoVia = new SondaTipoVia(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaTipoVia, arcMonitorTipoVia);

		this.sondaTraficoVia = new SondaTraficoVia(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaTraficoVia, arcMonitorTrafico);

		this.sondaAtencionConductor = new SondaAtencionConductor(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaAtencionConductor, arcMonitorAtencion);

		this.sondaAsientoConductor = new SondaAsientoConductorOcupado(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaAsientoConductor, arcMonitorAsientoCond);

		this.sondaAsientoCopiloto = new SondaAsientoCopilotoOcupado(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaAsientoCopiloto, arcMonitorAsientoCop);

		this.sondaNivelAutonomia = new SondaNivelAutonomia(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaNivelAutonomia, arcMonitorNivelAutonomia);

		this.sondaManosVolante = new SondaDeteccionManosVolante(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaManosVolante, arcMonitorManos);

		this.sondaFalloCritico = new SondaFalloCriticoSistema(bundleContext);
		BasicMAPEKLiteLoopHelper.deployProbe(this.sondaFalloCritico, arcMonitorFalloCritico);

		// ===== REGLAS DE ADAPTACIÓN =====
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaEnCarreteraEstandar(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarTipoViaAtasco(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarTipoViaCiudad(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarEstadoViaAutopista(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarEstadoViaCiudad(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarEstadoViaAutovia(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarSensorEnFallo(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaActivarFallbackPlan(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaPriorizacionSensores(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaAutoCuracionFalloCritico(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaAtencionConductor(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaManosVolante(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaUbicacionConductor(bundleContext));
		BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ReglaArranqueHighwayChauffer(bundleContext));

		// Arrancamos los hilos de medición de las sondas
		this.sondaModoConduccion.startMonitoring();
		this.sondaTipoVia.startMonitoring();
		this.sondaTraficoVia.startMonitoring();
		this.sondaAtencionConductor.startMonitoring();
		this.sondaAsientoConductor.startMonitoring();
		this.sondaAsientoCopiloto.startMonitoring();
		this.sondaNivelAutonomia.startMonitoring();
		this.sondaManosVolante.startMonitoring();
		this.sondaFalloCritico.startMonitoring();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		if (this.sondaModoConduccion != null)
			this.sondaModoConduccion.stopMonitoring();
		if (this.sondaTipoVia != null)
			this.sondaTipoVia.stopMonitoring();
		if (this.sondaTraficoVia != null)
			this.sondaTraficoVia.stopMonitoring();
		if (this.sondaAtencionConductor != null)
			this.sondaAtencionConductor.stopMonitoring();
		if (this.sondaAsientoConductor != null)
			this.sondaAsientoConductor.stopMonitoring();
		if (this.sondaAsientoCopiloto != null)
			this.sondaAsientoCopiloto.stopMonitoring();
		if (this.sondaNivelAutonomia != null)
			this.sondaNivelAutonomia.stopMonitoring();
		if (this.sondaManosVolante != null)
			this.sondaManosVolante.stopMonitoring();
		if (this.sondaFalloCritico != null)
			this.sondaFalloCritico.stopMonitoring();

		Activator.context = null;
	}

	/**
	 * Devuelve la KnowledgeProperty con el id dado, creándola si no existe. Si
	 * la creación falla, deja que la excepción se propague para que el error
	 * sea visible en la consola OSGi (no tragamos errores en silencio).
	 */
	private IKnowledgeProperty getOrCreateKP(String id) {
		IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty(id);
		if (kp != null)
			return kp;
		return BasicMAPEKLiteLoopHelper.createKnowledgeProperty(id);
	}

	private void setIfPresent(IKnowledgeProperty kp, Object value) {
		if (kp == null || value == null)
			return;
		if (kp.getValue() == null || !value.equals(kp.getValue()))
			kp.setValue(value);
	}

	protected IRuleComponentsSystemConfiguration createInitialSelfConfiguration() {
		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration("InitialConfiguration_" + ITimeStamped.getCurrentTimeStamp());

		// Configuración inicial: el coche arranca en modo manual (L0).
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L0_MANUAL, ConfiguracionHelper.V);

		return cfg;
	}
}
