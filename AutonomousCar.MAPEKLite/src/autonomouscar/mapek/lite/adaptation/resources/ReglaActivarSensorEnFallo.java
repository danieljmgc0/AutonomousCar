package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.Bundle;
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

/**
 * Requisito ADS_L3-7 (regla-activar-sensor-en-fallo).
 *
 * Estando en L3 y siendo el RightDistanceSensor inalcanzable mientras el LIDAR
 * sí está disponible, se sustituye RightDistanceSensor por LIDAR en el L3
 * activo. Esta regla SÓLO sustituye sensores: si no hay sustitución posible
 * NO desmantela L3 — la curación de fallo sistémico es responsabilidad de la
 * regla ADS-2 (regla-auto-curacion-fallo-critico-l0-manual).
 */
public class ReglaActivarSensorEnFallo extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaActivarSensorEnFallo.class);
	public static String ID = "regla-activar-sensor-en-fallo";

	private BundleContext bundleContext;

	IKnowledgeProperty kp_nivelAutonomia = null;
	IKnowledgeProperty kp_modo = null;

	public ReglaActivarSensorEnFallo(BundleContext context) {
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

		boolean rightDistanceAvailable = isBundleActive(ConfiguracionHelper.DEV_RIGHT_DIST);
		boolean lidarAvailable = isBundleActive(ConfiguracionHelper.DEV_LIDAR);

		if (rightDistanceAvailable || !lidarAvailable)
			throw new RuleException(
					"RightDistanceSensor disponible o LIDAR no disponible — no procede sustitución",
					"No ejecutamos la regla");

		String l3Activo = resolveL3(modo);
		if (l3Activo == null)
			throw new RuleException("No hay servicio L3 activo identificado", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Sustitución: quitar RightDistanceSensor, vincular LIDAR en su lugar.
		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DEV_RIGHT_DIST, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DEV_LIDAR, ConfiguracionHelper.V);

		SystemConfigurationHelper.bindingToRemove(cfg, l3Activo, ConfiguracionHelper.V,
				ConfiguracionHelper.REQ_RIGHT_DIST, ConfiguracionHelper.DEV_RIGHT_DIST, ConfiguracionHelper.V,
				ConfiguracionHelper.PROVIDED_SENSOR);
		SystemConfigurationHelper.bindingToAdd(cfg, l3Activo, ConfiguracionHelper.V,
				ConfiguracionHelper.REQ_RIGHT_DIST, ConfiguracionHelper.DEV_LIDAR, ConfiguracionHelper.V,
				ConfiguracionHelper.PROVIDED_SENSOR);

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

	private boolean isBundleActive(String symbolicName) {
		if (symbolicName == null)
			return false;
		for (Bundle b : this.bundleContext.getBundles()) {
			if (symbolicName.equals(b.getSymbolicName()))
				return b.getState() == Bundle.ACTIVE;
		}
		return false;
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
