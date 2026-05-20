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
 * ADS_L3-6: Actualmente en CityChauffer y pasa el estado de la vía a autopista => HighwayChauffer (fluido) o TrafficJam (atasco)
 */
public class ReglaActivarEstadoViaAutovia extends AdaptationRule {

	public static String ID = "regla-activar-estado-via-autovia";

	IKnowledgeProperty kp_modo;
	IKnowledgeProperty kp_tipo;
	IKnowledgeProperty kp_trafico;

	public ReglaActivarEstadoViaAutovia(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("modo-conduccion");
		this.setListenToKnowledgePropertyChanges("tipo-via");
		this.setListenToKnowledgePropertyChanges("trafico-via");
		kp_modo    = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
		kp_tipo    = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
		kp_trafico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_modo == null || kp_tipo == null || kp_trafico == null) return false;
		if (kp_modo.getValue() == null || kp_tipo.getValue() == null || kp_trafico.getValue() == null) return false;
		return "L3_CityChauffer".equals(kp_modo.getValue())
				&& "Autopista".equals(kp_tipo.getValue());
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());
		ConfiguracionHelper.removeL3CityChauffer(config);
		if ("Fluido".equals(kp_trafico.getValue())) {
			ConfiguracionHelper.addL3HighwayChauffer(config);
		} else {
			ConfiguracionHelper.addL3TrafficJamChauffer(config);
		}
		return config;
	}
}
