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
 * Regla de arranque de L3_HighwayChauffer.
 *
 * Cuando circulamos por autopista con tráfico fluido y aún no estamos en
 * L3_HighwayChauffer, conmutamos a ese modo. Las Sondas de modo-conduccion y
 * nivel-autonomia detectarán el nuevo servicio activo y actualizarán sus KPs.
 *
 * Condición:
 *   tipo-via    == "Autopista"
 *   trafico-via == "Fluido"
 *   modo-conduccion != "L3_HighwayChauffer"
 */
public class ReglaArranqueHighwayChauffer extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ReglaArranqueHighwayChauffer.class);
	public static String ID = "regla-arranque-highway-chauffer";

	IKnowledgeProperty kp_tipoVia = null;
	IKnowledgeProperty kp_trafico = null;
	IKnowledgeProperty kp_modo = null;

	public ReglaArranqueHighwayChauffer(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("tipo-via");
		this.setListenToKnowledgePropertyChanges("trafico-via");
		this.setListenToKnowledgePropertyChanges("modo-conduccion");

		kp_tipoVia = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
		kp_trafico = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
		kp_modo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_tipoVia == null || kp_trafico == null || kp_modo == null) {
			logger.trace("Required Knowledge property not set.");
			return false;
		}
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {

		String tipo = (String) kp_tipoVia.getValue();
		String trafico = (String) kp_trafico.getValue();
		String modo = (String) kp_modo.getValue();
		if (tipo == null || trafico == null || modo == null)
			throw new RuleException("KP nulos", "No ejecutamos la regla");

		if (!"Autopista".equals(tipo) || !"Fluido".equals(trafico))
			throw new RuleException("Condición no aplicable", "No ejecutamos la regla");

		if ("L3_HighwayChauffer".equals(modo))
			throw new RuleException("Ya estamos en L3_HighwayChauffer", "No ejecutamos la regla");

		IRuleComponentsSystemConfiguration cfg = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		// Quitar cualquier modo de conducción inferior o L3 distinto
		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DRV_L0_MANUAL, ConfiguracionHelper.V);
		ConfiguracionHelper.removeL1(cfg);
		ConfiguracionHelper.removeAnyL2(cfg);
		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DRV_L3_TRAFFICJAM, ConfiguracionHelper.V);
		SystemConfigurationHelper.componentToRemove(cfg, ConfiguracionHelper.DRV_L3_CITY, ConfiguracionHelper.V);

		// Añadir L3_HighwayChauffer con todas sus dependencias
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY, ConfiguracionHelper.V);
		ConfiguracionHelper.wireL3(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY);

		// Vincular fallback plan de emergencia por defecto (ADS_L3-8 puede
		// afinarlo a ParkInTheRoadShoulder después según disponibilidad de sensores).
		SystemConfigurationHelper.componentToAdd(cfg, ConfiguracionHelper.FB_EMERGENCY, ConfiguracionHelper.V);
		ConfiguracionHelper.wireFallbackEmergency(cfg);
		SystemConfigurationHelper.bindingToAdd(cfg, ConfiguracionHelper.DRV_L3_HIGHWAY, ConfiguracionHelper.V,
				ConfiguracionHelper.REQ_FALLBACKPLAN, ConfiguracionHelper.FB_EMERGENCY, ConfiguracionHelper.V,
				ConfiguracionHelper.PROVIDED_DRIVINGSERVICE);

		return cfg;
	}

}
