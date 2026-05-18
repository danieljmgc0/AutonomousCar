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
 * Requisito ADS-2 (regla-auto-curacion-fallo-critico-l0-manual).
 *
 * En caso de fallo sistémico general (fallo-critico-sistema = true) o que no
 * exista configuración posible, el sistema debe activar L0_ManualDriving.
 */
public class ReglaAutoCuracionFalloCritico extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaAutoCuracionFalloCritico.class);
	public static String ID = "regla-auto-curacion-fallo-critico-l0-manual";

	IKnowledgeProperty kp_falloCritico = null;

	public ReglaAutoCuracionFalloCritico(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("fallo-critico-sistema");

		kp_falloCritico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fallo-critico-sistema");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_falloCritico == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		Boolean fallo = (Boolean) kp_falloCritico.getValue();
		if (fallo == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (!Boolean.TRUE.equals(fallo))
			throw new RuleException("No hay fallo crítico", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Sólo dejamos L0_ManualDriving activo
		ConfiguracionHelper.removeAnyL3(cfg);
		ConfiguracionHelper.removeAnyL2(cfg);
		ConfiguracionHelper.removeL1(cfg);

		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L0_MANUAL, ConfiguracionHelper.V);

		return cfg;
	}

}
