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

/**
 * INTERACT-2: Ajusta mecanismos de notificación según si el conductor tiene las
 * manos en el volante (L3).
 * Con manos   → solo vibración volante (si lo requiere)
 * Sin manos   → resto de mecanismos (sin vibración volante)
 * Sin asiento → sin vibración asiento ni consola conductor
 */
public class ReglaCambioManosVolante extends AdaptationRule {

	public static String ID = "regla-cambio-situacion-manos-volante";

	IKnowledgeProperty kp_nivel;
	IKnowledgeProperty kp_manos;
	IKnowledgeProperty kp_asiento;

	public ReglaCambioManosVolante(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("deteccion-manos-volante");
		kp_nivel   = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_manos   = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("deteccion-manos-volante");
		kp_asiento = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("asiento-conductor-ocupado");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivel == null || kp_manos == null) return false;
		if (kp_nivel.getValue() == null || kp_manos.getValue() == null) return false;
		return (Integer) kp_nivel.getValue() == 3;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		ConfiguracionHelper.removeAllMechanisms(config);

		boolean manosEnVolante = Boolean.TRUE.equals(kp_manos.getValue());
		boolean asientoConductor = !Boolean.FALSE.equals(
				kp_asiento != null ? kp_asiento.getValue() : Boolean.TRUE);

		if (manosEnVolante) {
			// Solo vibración en el volante
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_STEERING_WHEEL);
		} else if (asientoConductor) {
			// Sin vibración en volante, con vibración asiento
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DASHBOARD_TEXT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_ICON);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_SOUND);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_BEEP);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_TEXT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DASHBOARD_ICON);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SEAT_DRIVER);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SEAT_COPILOT);
		} else {
			// Sin volante ni asiento conductor
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DASHBOARD_TEXT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_ICON);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_SOUND);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_BEEP);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_TEXT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DASHBOARD_ICON);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SEAT_COPILOT);
		}
		return config;
	}
}
