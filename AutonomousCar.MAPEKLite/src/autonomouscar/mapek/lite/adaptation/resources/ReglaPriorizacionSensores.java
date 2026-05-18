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
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import sua.autonomouscar.devices.interfaces.IDistanceSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;

/**
 * Requisito ADS-1 (regla-priorizacion-sensores).
 *
 * Cuando un servicio L1/L2/L3 esté activo, debe usar los sensores más
 * preferentes disponibles. Si el RightDistanceSensor no está pero el LIDAR sí,
 * el ADS pasa a usar LIDAR (mayor fiabilidad).
 */
public class ReglaPriorizacionSensores extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaPriorizacionSensores.class);
	public static String ID = "regla-priorizacion-sensores";

	private BundleContext bundleContext;

	IKnowledgeProperty kp_nivelAutonomia = null;
	IKnowledgeProperty kp_modo = null;

	public ReglaPriorizacionSensores(BundleContext context) {
		super(context, ID);
		this.bundleContext = context;
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("modo-conduccion");

		kp_nivelAutonomia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_modo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivelAutonomia == null || kp_modo == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		Integer nivel = toInt(kp_nivelAutonomia.getValue());
		String modo = (String) kp_modo.getValue();
		if (nivel == null || modo == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (nivel < 1)
			throw new RuleException("Sin ADS activo", "No ejecutamos la regla");

		boolean rightDistOk = OSGiUtils.getService(this.bundleContext, IDistanceSensor.class) != null;
		boolean lidarOk = this.bundleContext.getBundle(ConfiguracionHelper.DEV_LIDAR) != null;

		if (rightDistOk || !lidarOk)
			throw new RuleException("Sensor preferente ya en uso o LIDAR no disponible", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DEV_LIDAR, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DEV_RIGHT_DIST, ConfiguracionHelper.V);

		String activo = resolveActiveDrivingId(modo);
		if (activo != null) {
			SystemConfigurationHelper.bindingToRemove(cfg, activo, ConfiguracionHelper.V,
					ConfiguracionHelper.REQ_RIGHT_DIST, ConfiguracionHelper.DEV_RIGHT_DIST, ConfiguracionHelper.V,
					ConfiguracionHelper.PROVIDED_SENSOR);
			SystemConfigurationHelper.bindingToAdd(cfg, activo, ConfiguracionHelper.V,
					ConfiguracionHelper.REQ_RIGHT_DIST, ConfiguracionHelper.DEV_LIDAR, ConfiguracionHelper.V,
					ConfiguracionHelper.PROVIDED_SENSOR);
		}

		return cfg;
	}

	private String resolveActiveDrivingId(String modo) {
		switch (modo) {
		case "L3_HighwayChauffer":
			return ConfiguracionHelper.DRV_L3_HIGHWAY;
		case "L3_TrafficJamChauffer":
			return ConfiguracionHelper.DRV_L3_TRAFFICJAM;
		case "L3_CityChauffer":
			return ConfiguracionHelper.DRV_L3_CITY;
		case "L2_AdaptiveCruiseControl":
			return ConfiguracionHelper.DRV_L2_ACC;
		case "L2_LaneKeepingAssist":
			return ConfiguracionHelper.DRV_L2_LKA;
		default:
			return null;
		}
	}

	private Integer toInt(Object v) {
		if (v == null)
			return null;
		if (v instanceof Integer)
			return (Integer) v;
		if (v instanceof Number)
			return Integer.valueOf(((Number) v).intValue());
		try {
			return Integer.valueOf(v.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
