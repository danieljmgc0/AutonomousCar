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
 * ADS_L3-5: TrafficJamChauffer + atasco + ciudad → CityChauffer.
 */
public class ReglaActivarEstadoViaCiudad extends AdaptationRule {

	public static String ID = "regla-activar-estado-via-ciudad";

	IKnowledgeProperty kp_modo;
	IKnowledgeProperty kp_trafico;
	IKnowledgeProperty kp_tipo;

	public ReglaActivarEstadoViaCiudad(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("modo-conduccion");
		this.setListenToKnowledgePropertyChanges("trafico-via");
		this.setListenToKnowledgePropertyChanges("tipo-via");
		kp_modo    = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
		kp_trafico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
		kp_tipo    = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_modo == null || kp_trafico == null || kp_tipo == null) return false;
		if (kp_modo.getValue() == null || kp_trafico.getValue() == null || kp_tipo.getValue() == null) return false;
		return "L3_TrafficJamChauffer".equals(kp_modo.getValue())
				&& "Atasco".equals(kp_trafico.getValue())
				&& "Ciudad".equals(kp_tipo.getValue());
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());
		ConfiguracionHelper.removeL3TrafficJamChauffer(config);
		ConfiguracionHelper.addL3CityChauffer(config);
		return config;
	}
}
