/*
* Author: Mookeun Ji, goofcode@gmail.com
* 
* This source provides 2 external functions of initMotor() and updateMotorSpeed().
*
* initMotor() readies the motors to be run on the main loop(loop()).
*  # On my project, me and my team members found the fact that analogWrite() doesn't work properly 
*  # UNLESS we run the motor at certain power level(parameter) for a few seconds.
*  # We assume this is the motor issue, not of function itself.
* 
* updateMotorSpeed() clamps motor_speed to the value between minimum and maximum of motor speed
*  				 and writes clamped speed values to motor pins. 
* This function actually runs motor at the power of motor_speed by calling analogWrite().
*/


const int motor_pin[4] = {10, 9, 11, 3};
const float MAX_SPEED = 254;

float motor_speed[4];

void initMotor() {
	for(int i =0; i<4; i++){
		motor_speed[i] = 0;
		analogWrite(motor_pin[i], motor_speed[i]);
	}
}

void updateMotorSpeed() {
	for(int i=0; i<4; i++){
  		if(throttle == 0 || motor_speed[i] < 0) motor_speed[i] = 0;
		if(motor_speed[i] > MAX_SPEED) motor_speed[i] = MAX_SPEED;

		analogWrite(motor_pin[i], motor_speed[i]);
	}
}
