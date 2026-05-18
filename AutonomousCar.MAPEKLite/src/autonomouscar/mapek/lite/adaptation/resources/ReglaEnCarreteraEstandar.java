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
import sua.autonomouscar.driving.interfaces.IL2_AdaptiveCruiseControl;
import sua.autonomouscar.infraestructure.OSGiUtils;

/**
 * Requisito ADS_L3-1 (regla-en-carretera-estandar).
 *
 * Si estando en L3 entramos a Ciudad u OffRoad, hay que desactivar L3 y
 * pasar a L2 ACC si está disponible; en otro caso, a L1 Manual Asistida.
 */
public class ReglaEnCarreteraEstandar extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaEnCarreteraEstandar.class);
	public static String ID = "regla-en-carretera-estandar";

	private BundleContext bundleContext;

	IKnowledgeProperty kp_nivelAutonomia = null;
	IKnowledgeProperty kp_tipoVia = null;

	public ReglaEnCarreteraEstandar(BundleContext context) {
		super(context, ID);
		this.bundleContext = context;
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("tipo-via");

		kp_nivelAutonomia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_tipoVia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivelAutonomia == null || kp_tipoVia == null) {
			logger.trace("Required Knowledge property not set. Not executing the rule ...");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		Integer nivel = toInt(kp_nivelAutonomia.getValue());
		String tipo = (String) kp_tipoVia.getValue();
		if (nivel == null || tipo == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (nivel != 3 || (!"Ciudad".equals(tipo) && !"OffRoad".equals(tipo)))
			throw new RuleException("Condición no aplicable", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		ConfiguracionHelper.removeAnyL3(cfg);

		IL2_AdaptiveCruiseControl acc = OSGiUtils.getService(this.bundleContext, IL2_AdaptiveCruiseControl.class);
		if (acc != null) {
			// Cuerpo A: L2_AdaptiveCruiseControl disponible
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L2_ACC, ConfiguracionHelper.V);
			ConfiguracionHelper.wireL2(cfg, ConfiguracionHelper.DRV_L2_ACC);
		} else {
			// Cuerpo B: L1 Manual Asistida
			ConfiguracionHelper.removeAnyL2(cfg);
			SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L1_ASSISTED, ConfiguracionHelper.V);
			ConfiguracionHelper.wireL1(cfg);
		}

		return cfg;
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
