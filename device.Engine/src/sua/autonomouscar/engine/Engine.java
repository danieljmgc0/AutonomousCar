package sua.autonomouscar.engine;

import sua.autonomouscar.devices.interfaces.IEngine;

public class Engine implements IEngine{

    private static final int MIN_RPM = 0;
    private static final int MAX_RPM = 6000;
	protected int rpm = 0;

    @Override
    public IEngine accelerate(int rpm) {

        this.rpm += rpm;
        if (this.rpm > MAX_RPM) {
            this.rpm = MAX_RPM;
        }

        System.out.println("[Engine] Accelerating to " + this.rpm + " rpm");

        return this;
    }

    @Override
    public IEngine decelerate(int rpm) {

        this.rpm -= rpm;

        if (this.rpm < MIN_RPM) {
            this.rpm = MIN_RPM;
        }
        
        System.out.println("[Engine] Decelerating to " + this.rpm + " rpm");

        return this;
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
