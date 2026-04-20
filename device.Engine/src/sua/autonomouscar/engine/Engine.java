package sua.autonomouscar.engine;

import sua.autonomouscar.devices.interfaces.IEngine;

public class Engine implements IEngine{

	protected int rpm = 0;
	@Override
	public IEngine accelerate(int rpm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEngine decelerate(int rpm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEngine setRPM(int rpm) {
		this.rpm = rpm;
		System.out.println("[Engine] Setting to " + rpm + " rpm");
		return this;
	}

	@Override
	public int getCurrentRPM() {
		return this.rpm;
	}

}
