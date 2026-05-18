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
 * Requisito ADS_L3-6 (regla-activar-estado-via-autovia).
 *
 * Cuerpo A: L3_CityChauffer + Autopista + Fluido -> L3_HighwayChauffer.
 * Cuerpo B: L3_CityChauffer + Autopista + Atasco -> L3_TrafficJamChauffer.
 */
public class ReglaActivarEstadoViaAutovia extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaActivarEstadoViaAutovia.class);
	public static String ID = "regla-activar-estado-via-autovia";

	IKnowledgeProperty kp_modo = null;
	IKnowledgeProperty kp_tipoVia = null;
	IKnowledgeProperty kp_trafico = null;

	public ReglaActivarEstadoViaAutovia(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("modo-conduccion");
		this.setListenToKnowledgePropertyChanges("tipo-via");
		this.setListenToKnowledgePropertyChanges("trafico-via");

		kp_modo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
		kp_tipoVia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
		kp_trafico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_modo == null || kp_tipoVia == null || kp_trafico == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		String modo = (String) kp_modo.getValue();
		String tipo = (String) kp_tipoVia.getValue();
		String trafico = (String) kp_trafico.getValue();
		if (modo == null || tipo == null || trafico == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (!"L3_CityChauffer".equals(modo) || !"Autopista".equals(tipo))
			throw new RuleException("Condición no aplicable", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DRV_L3_CITY, ConfiguracionHelper.V);

		if ("Fluido".equals(trafico)) {
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY, ConfiguracionHelper.V);
			ConfiguracionHelper.wireL3(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY);
		} else if ("Atasco".equals(trafico)) {
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L3_TRAFFICJAM, ConfiguracionHelper.V);
			ConfiguracionHelper.wireL3(cfg, ConfiguracionHelper.DRV_L3_TRAFFICJAM);
		} else {
			throw new RuleException("Tráfico desconocido", "No ejecutamos la regla");
		}

		return cfg;
	}

}
