package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.Bundle;
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
 * ADS_L3-1 (regla-en-carretera-estandar):
 *Estando en conducción autónoma nivel 3 se pasa a tipo de vía carretera estándar u off-road
 *El ADS  desactiva conducción autónoma nivel 3 y activa nivel 2 (si es posible). Si no, activará nivel 1
 */
public class ReglaEnCarreteraEstandar extends AdaptationRule {

	public static String ID = "regla-en-carretera-estandar";

	private static final String BUNDLE_L2 = "driving.L2.AdaptiveCruiseControl";

	IKnowledgeProperty kp_nivelAutonomia;
	IKnowledgeProperty kp_tipoVia;
	IKnowledgeProperty kp_modoCond;
	private BundleContext context;

	public ReglaEnCarreteraEstandar(BundleContext context) {
		super(context, ID);
		this.context = context;
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		this.setListenToKnowledgePropertyChanges("tipo-via");
		kp_nivelAutonomia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_tipoVia        = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
		kp_modoCond       = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivelAutonomia == null || kp_tipoVia == null) return false;
		Object tipo = kp_tipoVia.getValue();
		return Integer.valueOf(3).equals(kp_nivelAutonomia.getValue())
			&& ("Carretera".equals(tipo) || "OffRoad".equals(tipo));
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Quitar servicio L3 actualmente
		String modoActual = (kp_modoCond != null) ? (String) kp_modoCond.getValue() : null;
		if ("L3_HighwayChauffer".equals(modoActual)) {
			ConfiguracionHelper.removeL3HighwayChauffer(config);
		} else if ("L3_CityChauffer".equals(modoActual)) {
			ConfiguracionHelper.removeL3CityChauffer(config);
		} else if ("L3_TrafficJamChauffer".equals(modoActual)) {
			ConfiguracionHelper.removeL3TrafficJamChauffer(config);
		} else {
			ConfiguracionHelper.removeAllL3(config);
		}

		// Comprobar disponiblidad del bundle L2 en OSGi, es decir, su estado es ACTIVE
		boolean l2Disponible = false;
		for (Bundle b : this.context.getBundles()) {
			if (BUNDLE_L2.equals(b.getSymbolicName()) && b.getState() == Bundle.ACTIVE) {
				l2Disponible = true;
				break;
			}
		}

		if (l2Disponible) {
			ConfiguracionHelper.addL2AdaptiveCruiseControl(config);
		} else {
			ConfiguracionHelper.addL1AssistedDriving(config);
		}
		return config;
	}
}
