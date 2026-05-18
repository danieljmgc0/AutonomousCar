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
 * Requisito INTERACT-2 (regla-cambio-situacion-manos-volante).
 *
 * - Manos en el volante: sólo vibración de volante.
 * - Sin manos en el volante: el resto de mecanismos (sin vibración del volante)
 *   incluyendo vibración del asiento conductor.
 * - Conductor no en el asiento: como el anterior pero sin vibración de asiento
 *   conductor (sí asiento copiloto).
 */
public class ReglaManosVolante extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaManosVolante.class);
	public static String ID = "regla-cambio-situacion-manos-volante";

	IKnowledgeProperty kp_nivelAutonomia = null;
	IKnowledgeProperty kp_manos = null;
	IKnowledgeProperty kp_asientoConductor = null;

	public ReglaManosVolante(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("deteccion-manos-volante");
		this.setListenToKnowledgePropertyChanges("asiento-conductor-ocupado");

		kp_nivelAutonomia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_manos = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("deteccion-manos-volante");
		kp_asientoConductor = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("asiento-conductor-ocupado");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivelAutonomia == null || kp_manos == null || kp_asientoConductor == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		Integer nivel = toInt(kp_nivelAutonomia.getValue());
		Boolean manos = (Boolean) kp_manos.getValue();
		Boolean asientoConductor = (Boolean) kp_asientoConductor.getValue();
		if (nivel == null || manos == null || asientoConductor == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (nivel != 3)
			throw new RuleException("No estamos en L3", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		if (Boolean.TRUE.equals(manos)) {
			// Condición 1: sólo vibración de volante
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_STEERINGWHEEL);
			ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_DRIVER);
			ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_COPILOT);
			return cfg;
		}

		// Condición 2 / 3: sin manos en el volante → quitar vibración volante,
		// usar el resto de mecanismos según ocupación del asiento conductor.
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_STEERINGWHEEL);

		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DASH_TEXT, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DASH_ICON, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_TEXT, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_ICON, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SPEAKERS_BEEP, ConfiguracionHelper.V);
		ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DASH_TEXT);
		ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DASH_ICON);
		ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_TEXT);
		ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_ICON);
		ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND);
		ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_BEEP);

		if (Boolean.TRUE.equals(asientoConductor)) {
			// Conductor en asiento → vibración asiento conductor permitida
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SEAT_DRIVER, ConfiguracionHelper.V);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_DRIVER);
			ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_COPILOT);
		} else {
			// Conductor fuera del asiento → vibración asiento copiloto
			ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_DRIVER);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SEAT_COPILOT, ConfiguracionHelper.V);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_COPILOT);
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
