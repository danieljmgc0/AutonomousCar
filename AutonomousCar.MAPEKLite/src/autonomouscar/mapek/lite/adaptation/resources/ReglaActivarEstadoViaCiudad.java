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
 * Requisito ADS_L3-5 (regla-activar-estado-via-ciudad).
 *
 * L3_TrafficJamChauffer + Atasco + Ciudad -> L3_CityChauffer.
 */
public class ReglaActivarEstadoViaCiudad extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaActivarEstadoViaCiudad.class);
	public static String ID = "regla-activar-estado-via-ciudad";

	IKnowledgeProperty kp_modo = null;
	IKnowledgeProperty kp_trafico = null;
	IKnowledgeProperty kp_tipoVia = null;

	public ReglaActivarEstadoViaCiudad(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("modo-conduccion");
		this.setListenToKnowledgePropertyChanges("trafico-via");
		this.setListenToKnowledgePropertyChanges("tipo-via");

		kp_modo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
		kp_trafico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
		kp_tipoVia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_modo == null || kp_trafico == null || kp_tipoVia == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		String modo = (String) kp_modo.getValue();
		String trafico = (String) kp_trafico.getValue();
		String tipo = (String) kp_tipoVia.getValue();
		if (modo == null || trafico == null || tipo == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (!"L3_TrafficJamChauffer".equals(modo) || !"Atasco".equals(trafico) || !"Ciudad".equals(tipo))
			throw new RuleException("Condición no aplicable", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DRV_L3_TRAFFICJAM, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L3_CITY, ConfiguracionHelper.V);
		ConfiguracionHelper.wireL3(cfg, ConfiguracionHelper.DRV_L3_CITY);

		return cfg;
	}

}
