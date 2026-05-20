package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorPosibilidadConduccion extends Monitor {

	public static String ID = "monitor-posibilidad-conduccion";

	public MonitorPosibilidadConduccion(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			Boolean valor = (Boolean) measure;
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("posibilidad-conduccion");
			if (kp != null && !valor.equals(kp.getValue()))
				kp.setValue(valor);
		} catch (Exception e) {
			this.logger.error(String.format("Error en %s.report: %s", ID, e.toString()));
		}
		return this;
	}
}
