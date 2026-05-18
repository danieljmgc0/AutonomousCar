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
 * Requisito ADS_L3-3 (regla-activar-tipo-via-ciudad).
 *
 * L3_HighwayChauffer + Ciudad -> L3_CityChauffer.
 */
public class ReglaActivarTipoViaCiudad extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaActivarTipoViaCiudad.class);
	public static String ID = "regla-activar-tipo-via-ciudad";

	IKnowledgeProperty kp_modo = null;
	IKnowledgeProperty kp_tipoVia = null;

	public ReglaActivarTipoViaCiudad(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("modo-conduccion");
		this.setListenToKnowledgePropertyChanges("tipo-via");

		kp_modo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
		kp_tipoVia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_modo == null || kp_tipoVia == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		String modo = (String) kp_modo.getValue();
		String tipo = (String) kp_tipoVia.getValue();
		if (modo == null || tipo == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (!"L3_HighwayChauffer".equals(modo) || !"Ciudad".equals(tipo))
			throw new RuleException("Condición no aplicable", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L3_CITY, ConfiguracionHelper.V);
		ConfiguracionHelper.wireL3(cfg, ConfiguracionHelper.DRV_L3_CITY);

		return cfg;
	}

}
