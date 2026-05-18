package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorNivelAutonomia extends Monitor {

	public static String ID = "monitor-nivel-autonomia";
	public static String KNOWLEDGE_PROPERTY = "nivel-autonomia";

	public MonitorNivelAutonomia(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			Integer value = toInteger(measure);
			if (value == null)
				return this;
			if (value < 0)
				value = 0;
			if (value > 3)
				value = 3;

			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty(KNOWLEDGE_PROPERTY);
			if (kp != null && (kp.getValue() == null || !value.equals(kp.getValue()))) {
				kp.setValue(value);
			}
		} catch (Exception e) {
			return this;
		}
		return this;
	}

	private Integer toInteger(Object measure) {
		if (measure == null)
			return null;
		if (measure instanceof Integer)
			return (Integer) measure;
		if (measure instanceof Number)
			return Integer.valueOf(((Number) measure).intValue());
		try {
			return Integer.valueOf(measure.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
