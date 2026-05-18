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
 * INTERACT-1: Ajusta mecanismos de notificación según atención del conductor (L3).
 * Atento   → vibración volante + texto consola + icono consola
 * Dormido  → vibración volante + vibración asiento + altavoz sonido
 * Empanao  → vibración volante + icono consola + altavoz sonido + beep + texto consola
 */
public class ReglaCambioAtencionConductor extends AdaptationRule {

	public static String ID = "regla-cambio-situacion-atencion-conductor";

	IKnowledgeProperty kp_nivel;
	IKnowledgeProperty kp_atencion;

	public ReglaCambioAtencionConductor(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("atencion-conductor");
		kp_nivel   = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_atencion = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("atencion-conductor");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivel == null || kp_atencion == null) return false;
		if (kp_nivel.getValue() == null || kp_atencion.getValue() == null) return false;
		return (Integer) kp_nivel.getValue() == 3;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Limpiar todos los mecanismos actuales
		ConfiguracionHelper.removeAllMechanisms(config);

		String atencion = (String) kp_atencion.getValue();

		if ("Atento".equals(atencion)) {
			// Poco molestos: vibración volante + texto + icono en consola conductor
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_STEERING_WHEEL);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_TEXT);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_ICON);

		} else if ("Dormido".equals(atencion)) {
			// Muy molestos: vibración volante + vibración asiento + altavoz sonido
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_STEERING_WHEEL);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SEAT_DRIVER);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_SOUND);

		} else {
			// Empanao / no_atento: vibración volante + icono + sonido + beep + texto
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_STEERING_WHEEL);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_ICON);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_SOUND);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_SPEAKER_BEEP);
			ConfiguracionHelper.addMechanism(config, ConfiguracionHelper.MECH_DRIVER_TEXT);
		}
		return config;
	}
}
