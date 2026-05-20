package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorFalloCriticoSistema extends Monitor {

	public static String ID = "monitor-fallo-critico-sistema";

	public MonitorFalloCriticoSistema(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			Boolean falloCritico = (Boolean) measure;
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fallo-critico-sistema");
			if (kp != null && !falloCritico.equals(kp.getValue()))
				kp.setValue(falloCritico);
		} catch (Exception e) {
			this.logger.error(String.format("Error en %s.report: %s", ID, e.toString()));
		}
		return this;
	}
}
