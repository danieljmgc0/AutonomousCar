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

/**
 * Requisito INTERACT-3 (regla-cambio-situacion-ubicacion-conductor).
 *
 * - Conductor sentado: vibración del asiento conductor + consola del conductor.
 * - Conductor no sentado: sin vibración de asiento del conductor ni consola
 *   del conductor; resto de mecanismos según situación.
 */
public class ReglaUbicacionConductor extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaUbicacionConductor.class);
	public static String ID = "regla-cambio-situacion-ubicacion-conductor";

	IKnowledgeProperty kp_nivelAutonomia = null;
	IKnowledgeProperty kp_asientoConductor = null;

	public ReglaUbicacionConductor(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("asiento-conductor-ocupado");

		kp_nivelAutonomia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_asientoConductor = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("asiento-conductor-ocupado");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivelAutonomia == null || kp_asientoConductor == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		Integer nivel = toInt(kp_nivelAutonomia.getValue());
		Boolean ocupado = (Boolean) kp_asientoConductor.getValue();
		if (nivel == null || ocupado == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (nivel != 3)
			throw new RuleException("No estamos en L3", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		if (Boolean.TRUE.equals(ocupado)) {
			// Conductor sentado → DriverSeat vibración + DriverDisplay
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SEAT_DRIVER, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_TEXT, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_ICON, ConfiguracionHelper.V);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_DRIVER);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_TEXT);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_ICON);
		} else {
			// Conductor no sentado → desvincular DriverSeat y DriverDisplay,
			// usar Dashboard + Speakers + CopilotSeat + SteeringWheel
			ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_DRIVER);
			ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_TEXT);
			ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_ICON);

			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DASH_TEXT, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DASH_ICON, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SPEAKERS_BEEP, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SEAT_COPILOT, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_STEERINGWHEEL, ConfiguracionHelper.V);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DASH_TEXT);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DASH_ICON);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_BEEP);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_COPILOT);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_STEERINGWHEEL);
		}

		return cfg;
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
