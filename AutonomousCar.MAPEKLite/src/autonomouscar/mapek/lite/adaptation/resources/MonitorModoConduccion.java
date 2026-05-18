package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorModoConduccion extends Monitor {

	public static String ID = "monitor-modo-conduccion";

	public MonitorModoConduccion(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			String serviceId = (String) measure;

			int nivel;
			String modo;
			if (serviceId.startsWith("L3_")) {
				nivel = 3;
				modo  = serviceId;          // e.g. "L3_HighwayChauffer"
			} else if (serviceId.startsWith("L2_")) {
				nivel = 2;
				modo  = serviceId;          // e.g. "L2_AdaptiveCruiseControl"
			} else if (serviceId.startsWith("L1_")) {
				nivel = 1;
				modo  = serviceId;          // e.g. "L1_AssistedDriving"
			} else {
				nivel = 0;
				modo  = "L0_M";
			}

			IKnowledgeProperty kpNivel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("nivel-autonomia");
			if (kpNivel != null && !Integer.valueOf(nivel).equals(kpNivel.getValue()))
				kpNivel.setValue(nivel);

			IKnowledgeProperty kpModo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("modo-conduccion");
			if (kpModo != null && !modo.equals(kpModo.getValue()))
				kpModo.setValue(modo);

		} catch (Exception e) { /* ignorar */ }
		return this;
	}
}
