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
 * Requisito INTERACT-1 (regla-cambio-situacion-atencion-conductor).
 *
 * Cuando estando en L3 cambia la atención del conductor, se asignan distintos
 * mecanismos de interacción al NotificationService:
 *  - Atento: vibración de volante + iconos/texto en consola del conductor.
 *  - Dormido: vibración volante + asiento conductor + altavoces (sonido).
 *  - Empanao (no atento): mecanismos medianamente molestos (texto/icono,
 *    sonido/beep, vibración volante).
 */
public class ReglaAtencionConductor extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaAtencionConductor.class);
	public static String ID = "regla-cambio-situacion-atencion-conductor";

	IKnowledgeProperty kp_nivelAutonomia = null;
	IKnowledgeProperty kp_atencion = null;

	public ReglaAtencionConductor(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("atencion-conductor");

		kp_nivelAutonomia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_atencion = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("atencion-conductor");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivelAutonomia == null || kp_atencion == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		Integer nivel = toInt(kp_nivelAutonomia.getValue());
		String atencion = (String) kp_atencion.getValue();
		if (nivel == null || atencion == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (nivel != 3)
			throw new RuleException("No estamos en L3", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Limpiamos cualquier mecanismo previo y dejamos sólo los necesarios.
		desvincularTodosLosMecanismos(cfg);

		switch (atencion) {
		case "Atento":
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_STEERINGWHEEL, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_TEXT, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_ICON, ConfiguracionHelper.V);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_STEERINGWHEEL);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_TEXT);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_ICON);
			break;
		case "Dormido":
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_STEERINGWHEEL, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SEAT_DRIVER, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND, ConfiguracionHelper.V);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_STEERINGWHEEL);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_DRIVER);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND);
			break;
		case "Empanao":
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_STEERINGWHEEL, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_TEXT, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_DRIVER_ICON, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND, ConfiguracionHelper.V);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.INT_SPEAKERS_BEEP, ConfiguracionHelper.V);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_STEERINGWHEEL);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_TEXT);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_ICON);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND);
			ConfiguracionHelper.wireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_BEEP);
			break;
		default:
			throw new RuleException("Atención desconocida: " + atencion, "No ejecutamos la regla");
		}

		return cfg;
	}

	private void desvincularTodosLosMecanismos(IRuleComponentsSystemConfiguration cfg) {
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_STEERINGWHEEL);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_DRIVER);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SEAT_COPILOT);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_TEXT);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_DRIVER_ICON);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_DASH_TEXT);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_DASH_ICON);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_SOUND);
		ConfiguracionHelper.unwireInteractionMechanism(cfg, ConfiguracionHelper.INT_SPEAKERS_BEEP);
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
