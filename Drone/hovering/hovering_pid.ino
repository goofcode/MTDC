/* 	
* Author: Mookeun Ji, goofcode@gmail.com
* 
* This source provides 3 external functions of initPID(), calcPID() and calcMotorSpeed().
* 3 functions implement standard(or dual) PID control of quadcopter.
*
* initPID() sets target angles based on current angles read from the gyro sensor.
* 
* calcPID() calculates the PID outputs.
*
* calcMotorSpeed() calculates each motor speed from calcPID() outputs.
*/


enum {pitch, roll, yaw};

//output from YPR
extern float dt;
extern float filtered_angle[3];
extern float gyro[3];

float base_target_angle[3] = {0.0, 0.0, 0.0};
float target_angle[3] = {0.0, 0.0, 0.0};

float output[3];

extern float motor_speed[4];

//init pid
void initPID() {
	//set base_target_angle
	for (int i = 0; i < 10; i++) {
		calcYRP();

		for (int i = 0; i < 3; i++)
			base_target_angle[i] += filtered_angle[i] / 10;
		delay(100);
	}

	for (int i = 0; i < 3; i++)
		target_angle[i] = base_target_angle[i];
}

/*
float uni_kp = 0.6;

float stabilize_kp[3] = {uni_kp, uni_kp, uni_kp};
float stabilize_ki[3] = {0, 0, 0};
float rate_kp[3] = {uni_kp, uni_kp, uni_kp};
float rate_ki[3] = {0, 0, 0};
float stabilize_iterm[3], rate_iterm[3];
*/
void calcPID() {
/*
	//dual PID
	float* angle_in = filtered_angle;
	float* rate_in = gyro;

	// for each roll pitch yaw
	for (int i = 0; i < 3; i++)
	{
		//dual PID controll
		float angle_error = target_angle[i] - angle_in[i];

		float stabilize_pterm = stabilize_kp[i] * angle_error;
		stabilize_iterm[i] += stabilize_ki[i] * angle_error * dt;

		float desired_rate = stabilize_pterm;
		float rate_error = desired_rate - rate_in[i];

		float rate_pterm = rate_kp[i] * rate_error;
		rate_iterm[i] += rate_ki[i] * rate_error * dt;

		output[i] = rate_pterm + rate_iterm[i] + stabilize_iterm[i];
	}
*/
  
	//std PID
	const float ku = 1.0;
	const float tu = 0.7;

	const float kp = 0.6 * ku;
	const float ki = 2*kp/tu;
	const float kd = kp*tu/8;

	float pterm;
	static float iterm;
	float dterm;

  float* angle_in = filtered_angle;
	static float prev_input;

	for(int i=0; i<3; i++)
	{
		float error = target_angle[i] - angle_in[i];

		pterm = kp*error;
		//iterm += ki*error*dt;
		//dterm = -kd*((angle_in[i]-prev_input)/dt);
		//prev_input = angle_in[i];

		output[i] = pterm;// + iterm + dterm;
	}
}
void calcMotorSpeed() { 

	float yaw_output = 0;//output[yaw];
	float roll_output = output[roll];
	float pitch_output = output[pitch];

	float pid[4] = {
		yaw_output + roll_output + pitch_output,
		-yaw_output - roll_output + pitch_output,
		yaw_output - roll_output - pitch_output,
		-yaw_output + roll_output - pitch_output
	};

	for (int i = 0; i < 4; i++)
		motor_speed[i] = throttle;// + pid[i];
}

