package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import sua.autonomouscar.interfaces.ERoadType;

public class MonitorTipoVia extends Monitor {

	public static String ID = "monitor-tipo-via";

	public MonitorTipoVia(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			ERoadType roadType = (ERoadType) measure;
			String valor;
			switch (roadType) {
				case HIGHWAY:  valor = "Autopista"; break;
				case OFF_ROAD: valor = "OffRoad";   break;
				case CITY:     valor = "Ciudad";    break;
				case STD_ROAD: valor = "Carretera"; break;
				default: return this;
			}
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("tipo-via");
			if (kp != null && !valor.equals(kp.getValue()))
				kp.setValue(valor);
		} catch (Exception e) { /* ignorar */ }
		return this;
	}
}
