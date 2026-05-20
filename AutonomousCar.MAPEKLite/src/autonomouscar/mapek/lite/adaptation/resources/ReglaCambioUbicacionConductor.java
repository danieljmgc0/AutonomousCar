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
 * INTERACT-3: Ajusta mecanismos de notificación según ubicación del conductor (L3)
 * En asiento conductor => vibración asiento + texto consola + icono consola
 * Fuera del asiento    => dashboard + copiloto + altavoces (sin asiento/consola conductor)
 */
public class ReglaCambioUbicacionConductor extends AdaptationRule {

	public static String ID = "regla-cambio-situacion-ubicacion-conductor";

	IKnowledgeProperty kp_nivel;
	IKnowledgeProperty kp_asiento;

	public ReglaCambioUbicacionConductor(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("asiento-conductor-ocupado");
		kp_nivel   = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_asiento = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("asiento-conductor-ocupado");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivel == null || kp_asiento == null) return false;
		// Sólo se dispara en L3 y cuando el valor del asiento ya está disponible.
		return Integer.valueOf(3).equals(kp_nivel.getValue())
			&& kp_asiento.getValue() != null;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		ConfiguracionHelper.removeAllMechanisms(config);

		boolean asientoOcupado = Boolean.TRUE.equals(kp_asiento.getValue());

		if (asientoOcupado) {
			// Conductor en asiento → vibración asiento + consola conductor
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SEAT_DRIVER);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_TEXT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_ICON);
		} else {
			// Conductor fuera del asiento → dashboard + copiloto + altavoces
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DASHBOARD_TEXT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SEAT_COPILOT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_SOUND);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_BEEP);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DASHBOARD_ICON);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DASHBOARD_ICON2);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_STEERING_WHEEL);
		}
		return config;
	}
}
