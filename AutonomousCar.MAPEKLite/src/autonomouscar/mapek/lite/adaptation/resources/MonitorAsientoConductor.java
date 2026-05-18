package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorAsientoConductor extends Monitor {

	public static String ID = "monitor-asiento-conductor-ocupado";

	public MonitorAsientoConductor(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			Boolean valor = (Boolean) measure;
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("asiento-conductor-ocupado");
			if (kp != null && !valor.equals(kp.getValue()))
				kp.setValue(valor);
		} catch (Exception e) { /* ignorar */ }
		return this;
	}
}
