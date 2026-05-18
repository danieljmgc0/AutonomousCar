package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorSensorDerechoDisponible extends Monitor {

	public static String ID = "monitor-sensor-derecho-disponible";

	public MonitorSensorDerechoDisponible(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			Boolean disponible = (Boolean) measure;
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-derecho-disponible");
			if (kp != null && !disponible.equals(kp.getValue()))
				kp.setValue(disponible);
		} catch (Exception e) { /* ignorar */ }
		return this;
	}
}
