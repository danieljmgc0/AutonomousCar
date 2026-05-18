package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorFalloCriticoSistema extends Monitor {

	public static String ID = "monitor-fallo-critico-sistema";
	public static String KNOWLEDGE_PROPERTY = "fallo-critico-sistema";

	public MonitorFalloCriticoSistema(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			Boolean value = toBoolean(measure);
			if (value == null)
				return this;

			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty(KNOWLEDGE_PROPERTY);
			if (kp != null && (kp.getValue() == null || !value.equals(kp.getValue()))) {
				kp.setValue(value);
			}
		} catch (Exception e) {
			return this;
		}
		return this;
	}

	private Boolean toBoolean(Object measure) {
		if (measure == null)
			return null;
		if (measure instanceof Boolean)
			return (Boolean) measure;
		return Boolean.valueOf(measure.toString());
	}

}
