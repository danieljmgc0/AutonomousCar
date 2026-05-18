package autonomouscar.mapek.lite.adaptation.resources;

import java.util.List;

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
import sua.autonomouscar.devices.interfaces.ILineSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;

/**
 * Requisito ADS_L3-8 (regla-activar-fallback-plan).
 *
 * Al estar activa una función L3, debe vincularse un Fallback Plan:
 *  - 'aparcar en la cuneta' (preferente) si están disponibles los sensores
 *    necesarios (RightDistanceSensor + RightLineSensor).
 *  - 'emergencia' en caso contrario.
 */
public class ReglaActivarFallbackPlan extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaActivarFallbackPlan.class);
	public static String ID = "regla-activar-fallback-plan";

	private BundleContext bundleContext;

	IKnowledgeProperty kp_nivelAutonomia = null;
	IKnowledgeProperty kp_modo = null;

	public ReglaActivarFallbackPlan(BundleContext context) {
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

		if (nivel != 3)
			throw new RuleException("No estamos en L3", "No ejecutamos la regla");

		// Detectar disponibilidad de sensores necesarios para aparcar en la cuneta
		boolean rightDistOk = false;
		boolean rightLineOk = false;
		List<IDistanceSensor> distSensors = OSGiUtils.getServices(this.bundleContext, IDistanceSensor.class);
		List<ILineSensor> lineSensors = OSGiUtils.getServices(this.bundleContext, ILineSensor.class);
		if (distSensors != null && !distSensors.isEmpty())
			rightDistOk = true;
		if (lineSensors != null && !lineSensors.isEmpty())
			rightLineOk = true;

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		String l3Activo = resolveL3(modo);

		if (rightDistOk && rightLineOk) {
			// Cuerpo A: vincular ParkInTheRoadShoulder
			SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.FB_EMERGENCY, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.FB_PARK, ConfiguracionHelper.V);
			ConfiguracionHelper.wireFallbackPark(cfg);
			if (l3Activo != null) {
				SystemConfigurationHelper.bindingToAdd(cfg, l3Activo, ConfiguracionHelper.V,
						ConfiguracionHelper.REQ_FALLBACKPLAN, ConfiguracionHelper.FB_PARK, ConfiguracionHelper.V,
						ConfiguracionHelper.PROVIDED_DRIVINGSERVICE);
			}
		} else {
			// Cuerpo B: vincular Emergency
			SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.FB_PARK, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.FB_EMERGENCY, ConfiguracionHelper.V);
			ConfiguracionHelper.wireFallbackEmergency(cfg);
			if (l3Activo != null) {
				SystemConfigurationHelper.bindingToAdd(cfg, l3Activo, ConfiguracionHelper.V,
						ConfiguracionHelper.REQ_FALLBACKPLAN, ConfiguracionHelper.FB_EMERGENCY, ConfiguracionHelper.V,
						ConfiguracionHelper.PROVIDED_DRIVINGSERVICE);
			}
		}

		return cfg;
	}

	private String resolveL3(String modo) {
		if ("L3_HighwayChauffer".equals(modo))
			return ConfiguracionHelper.DRV_L3_HIGHWAY;
		if ("L3_TrafficJamChauffer".equals(modo))
			return ConfiguracionHelper.DRV_L3_TRAFFICJAM;
		if ("L3_CityChauffer".equals(modo))
			return ConfiguracionHelper.DRV_L3_CITY;
		return null;
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
