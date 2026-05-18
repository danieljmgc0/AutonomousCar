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
 *
 * SI nivel-autonomia==3 AND (tipo-via==Carretera OR tipo-via==OffRoad)
 *    AND L2 bundle ACTIVE  → desactivar L3 activo, activar L2_AdaptiveCruiseControl
 * ELSE IF nivel-autonomia==3 AND (tipo-via==Carretera OR tipo-via==OffRoad)
 *                           → desactivar L3 activo, activar L1_AssistedDriving
 *
 * posibilidad-conduccion es true cuando el bundle driving.L2.AdaptiveCruiseControl
 * está en estado ACTIVE en OSGi (su ARC está registrado).
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
		if (kp_nivelAutonomia.getValue() == null || kp_tipoVia.getValue() == null) return false;

		int nivel = (Integer) kp_nivelAutonomia.getValue();
		String tipo = (String) kp_tipoVia.getValue();
		// Condición: en L3 y entramos en vía no apta para ningún servicio L3
		// (Carretera estándar u off-road; Ciudad tiene su propio servicio L3_CityChauffer)
		return nivel == 3 && ("Carretera".equals(tipo) || "OffRoad".equals(tipo));
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Quitar el servicio L3 actualmente activo (identificado por modo-conduccion)
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

		// Comprobar en tiempo de ejecución si el bundle L2 está en estado ACTIVE en OSGi
		boolean l2Disponible = false;
		for (Bundle b : this.context.getBundles()) {
			if (BUNDLE_L2.equals(b.getSymbolicName()) && b.getState() == Bundle.ACTIVE) {
				l2Disponible = true;
				break;
			}
		}

		if (l2Disponible) {
			// SI nivel-autonomia==3 AND (Ciudad OR OffRoad) AND L2 bundle ACTIVE
			ConfiguracionHelper.addL2AdaptiveCruiseControl(config);
		} else {
			// ELSE: L2 no disponible → nivel 1 conducción manual asistida (ADS_L3-1)
			ConfiguracionHelper.addL1AssistedDriving(config);
		}
		return config;
	}
}
