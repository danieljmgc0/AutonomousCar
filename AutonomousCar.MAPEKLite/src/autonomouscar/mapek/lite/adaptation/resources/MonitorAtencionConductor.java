package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import sua.autonomouscar.interfaces.EFaceStatus;

public class MonitorAtencionConductor extends Monitor {

	public static String ID = "monitor-atencion-conductor";

	public MonitorAtencionConductor(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			EFaceStatus status = (EFaceStatus) measure;
			String valor;
			switch (status) {
				case LOOKING_FORWARD: valor = "Atento";  break;
				case DISTRACTED:      valor = "Empanao"; break;
				case SLEEPING:        valor = "Dormido"; break;
				default: return this;
			}
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("atencion-conductor");
			if (kp != null && !valor.equals(kp.getValue()))
				kp.setValue(valor);
		} catch (Exception e) { /* ignorar */ }
		return this;
	}
}
