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
 * ADS_L3-1: En autonomia L3 circulando por ciudad u off-road,
 * baja a L2_ACC (si disponible) o L1_AssistedDriving.
 */
public class ReglaEnCarreteraEstandar extends AdaptationRule {

	public static String ID = "regla-en-carretera-estandar";

	IKnowledgeProperty kp_nivelAutonomia;
	IKnowledgeProperty kp_tipoVia;
	IKnowledgeProperty kp_modoCond;
	IKnowledgeProperty kp_posibilidad;

	public ReglaEnCarreteraEstandar(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("tipo-via");
		kp_nivelAutonomia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_tipoVia        = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
		kp_modoCond       = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
		kp_posibilidad    = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("posibilidad-conduccion");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivelAutonomia == null || kp_tipoVia == null) return false;
		if (kp_nivelAutonomia.getValue() == null || kp_tipoVia.getValue() == null) return false;

		int nivel = (Integer) kp_nivelAutonomia.getValue();
		String tipo = (String) kp_tipoVia.getValue();
		return nivel == 3 && ("Ciudad".equals(tipo) || "OffRoad".equals(tipo));
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Quitar todos los servicios L3
		ConfiguracionHelper.removeAllL3(config);

		boolean l2Disponible = Boolean.TRUE.equals(
				kp_posibilidad != null ? kp_posibilidad.getValue() : null);

		if (l2Disponible) {
			ConfiguracionHelper.addL2AdaptiveCruiseControl(config);
		} else {
			ConfiguracionHelper.addL1AssistedDriving(config);
		}
		return config;
	}
}
