package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorTipoVia extends Monitor {

	public static String ID = "monitor-tipo-via";
	public static String KNOWLEDGE_PROPERTY = "tipo-via";

	public MonitorTipoVia(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			String raw = (measure == null) ? null : measure.toString().toUpperCase();
			String value = translate(raw);
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

	private String translate(String raw) {
		if (raw == null)
			return null;
		switch (raw) {
		case "HIGHWAY":
			return "Autopista";
		case "OFF_ROAD":
		case "OFFROAD":
			return "OffRoad";
		case "CITY":
		case "CIUDAD":
			return "Ciudad";
		case "STD_ROAD":
		case "STDROAD":
		case "CARRETERA":
			return "Carretera";
		default:
			return null;
		}
	}

}
