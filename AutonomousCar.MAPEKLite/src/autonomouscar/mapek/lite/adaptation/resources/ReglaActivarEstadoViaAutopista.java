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
 * Requisito ADS_L3-4 (regla-activar-estado-via-autopista).
 *
 * L3_TrafficJamChauffer + Fluido -> L3_HighwayChauffer.
 */
public class ReglaActivarEstadoViaAutopista extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaActivarEstadoViaAutopista.class);
	public static String ID = "regla-activar-estado-via-autopista";

	IKnowledgeProperty kp_modo = null;
	IKnowledgeProperty kp_trafico = null;

	public ReglaActivarEstadoViaAutopista(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("modo-conduccion");
		this.setListenToKnowledgePropertyChanges("trafico-via");

		kp_modo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
		kp_trafico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_modo == null || kp_trafico == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		String modo = (String) kp_modo.getValue();
		String trafico = (String) kp_trafico.getValue();
		if (modo == null || trafico == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (!"L3_TrafficJamChauffer".equals(modo) || !"Fluido".equals(trafico))
			throw new RuleException("Condición no aplicable", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DRV_L3_TRAFFICJAM, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY, ConfiguracionHelper.V);
		ConfiguracionHelper.wireL3(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY);

		return cfg;
	}

}
