package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import sua.autonomouscar.interfaces.ERoadStatus;

public class MonitorTraficoVia extends Monitor {

	public static String ID = "monitor-trafico-via";

	public MonitorTraficoVia(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			ERoadStatus status = (ERoadStatus) measure;
			String valor;
			switch (status) {
				case FLUID:     valor = "Fluido"; break;
				case JAM:
				case COLLAPSED: valor = "Atasco"; break;
				default: return this;
			}
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("trafico-via");
			if (kp != null && !valor.equals(kp.getValue()))
				kp.setValue(valor);
		} catch (Exception e) {
			this.logger.error(String.format("Error en %s.report: %s", ID, e.toString()));
		}
		return this;
	}
}
