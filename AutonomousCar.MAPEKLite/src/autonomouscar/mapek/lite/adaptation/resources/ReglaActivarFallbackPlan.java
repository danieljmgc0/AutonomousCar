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
import sua.autonomouscar.devices.interfaces.IDistanceSensor;
import sua.autonomouscar.devices.interfaces.ILineSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.infraestructure.driving.ARC.DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.L3_DrivingServiceARC;
import sua.autonomouscar.interfaces.IIdentifiable;

/**
 * ADS_L3-8: En L3, vincular siempre el FallbackPlan adecuado.
 * Preferente: ParkInTheRoadShoulder (si sensores disponibles).
 * Alternativa: Emergency.
 */
public class ReglaActivarFallbackPlan extends AdaptationRule {

	public static String ID = "regla-activar-fallback-plan";

	private BundleContext context;
	IKnowledgeProperty kp_nivel;
	IKnowledgeProperty kp_modo;

	public ReglaActivarFallbackPlan(BundleContext context) {
		super(context, ID);
		this.context = context;
		this.setListenToKnowledgePropertyChanges("nivel-autonomia");
		kp_nivel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
		kp_modo  = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_nivel == null || kp_nivel.getValue() == null) return false;
		return (Integer) kp_nivel.getValue() == 3;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
				.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		IDistanceSensor rightDs = OSGiUtils.getService(context, IDistanceSensor.class,
				"(" + IIdentifiable.ID + "=RightDistanceSensor)");
		ILineSensor rightLs = OSGiUtils.getService(context, ILineSensor.class,
				"(" + IIdentifiable.ID + "=RightLineSensor)");

		String dsActivo = kp_modo != null ? (String) kp_modo.getValue() : null;

		if (rightDs != null && rightLs != null && dsActivo != null) {
			// Preferente: aparcar en el arcén
			ConfiguracionHelper.addFallbackPlanParkInShoulder(config);
			// Conectar el ParkInShoulder como fallback al servicio L3 activo
			SystemConfigurationHelper.bindingToAdd(config,
					dsActivo, "1.0.0", L3_DrivingServiceARC.REQUIRED_FALLBACKPLAN,
					"driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", DrivingServiceARC.PROVIDED_DRIVINGSERVICE);
			// Emergency como respaldo secundario (ya debería estar en config)
			ConfiguracionHelper.addFallbackPlanEmergency(config);
		} else {
			// Solo emergencia
			ConfiguracionHelper.addFallbackPlanEmergency(config);
			if (dsActivo != null) {
				SystemConfigurationHelper.bindingToAdd(config,
						dsActivo, "1.0.0", L3_DrivingServiceARC.REQUIRED_FALLBACKPLAN,
						"driving.FallbackPlan.Emergency", "1.0.0", DrivingServiceARC.PROVIDED_DRIVINGSERVICE);
			}
		}
		return config;
	}
}
