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
 * ADS_L3-3: nivel-autonomia==3 AND tipo-via==Ciudad → L3_HighwayChauffer → L3_CityChauffer.
 */
public class ReglaActivarTipoViaCiudad extends AdaptationRule {

	public static String ID = "regla-activar-tipo-via-ciudad";

	IKnowledgeProperty kp_nivel;
	IKnowledgeProperty kp_tipo;
	IKnowledgeProperty kp_modo;

	public ReglaActivarTipoViaCiudad(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("tipo-via");
		kp_nivel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_tipo  = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
		kp_modo  = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivel == null || kp_tipo == null || kp_modo == null) return false;
		if (kp_nivel.getValue() == null || kp_tipo.getValue() == null) return false;
		return (Integer) kp_nivel.getValue() == 3
				&& "Ciudad".equals(kp_tipo.getValue())
				&& "L3_HighwayChauffer".equals(kp_modo.getValue());
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());
		ConfiguracionHelper.removeL3HighwayChauffer(config);
		ConfiguracionHelper.addL3CityChauffer(config);
		return config;
	}
}
