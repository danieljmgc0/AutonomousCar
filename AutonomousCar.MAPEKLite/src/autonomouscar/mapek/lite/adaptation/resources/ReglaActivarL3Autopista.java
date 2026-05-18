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
 * Regla de activación inicial de L3: cuando el coche circula por autopista
 * con tráfico fluido y aún no está en L3, activa L3_HighwayChauffer.
 * Cubre la transición L0 → L3 (o L1/L2 → L3).
 */
public class ReglaActivarL3Autopista extends AdaptationRule {

	public static String ID = "regla-activar-l3-autopista";

	IKnowledgeProperty kp_tipo;
	IKnowledgeProperty kp_trafico;
	IKnowledgeProperty kp_nivel;

	public ReglaActivarL3Autopista(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("tipo-via");
		this.setListenToKnowledgePropertyChanges("trafico-via");
		kp_tipo    = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
		kp_trafico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
		kp_nivel   = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_tipo == null || kp_trafico == null) return false;
		if (kp_tipo.getValue() == null || kp_trafico.getValue() == null) return false;

		boolean esAutopista = "Autopista".equals(kp_tipo.getValue());
		boolean esFluido    = "Fluido".equals(kp_trafico.getValue());
		Integer nivel       = (kp_nivel != null) ? (Integer) kp_nivel.getValue() : null;
		boolean noEsL3      = (nivel == null || nivel < 3);

		return esAutopista && esFluido && noEsL3;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Quitar servicio de conducción actual (L0, L1 o L2)
		SystemConfigurationHelper.componentToRemove(config, "driving.L1.AssitedDriving", "1.0.0");
		ConfiguracionHelper.removeL1AssistedDriving(config);
		ConfiguracionHelper.removeL2AdaptiveCruiseControl(config);

		// Activar L3_HighwayChauffer con todas sus dependencias
		ConfiguracionHelper.addL3HighwayChauffer(config);

		return config;
	}
}
