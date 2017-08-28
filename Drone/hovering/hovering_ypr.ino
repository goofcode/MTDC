/*
* Author: Mookeun Ji, goofcode@gmail.com
* 
* This source provides 2 external functions, initYPR() and calcYPR()
*
* initYPR() initiates gyro sensor and sets base gyro and acceleration for it. 
* Gyro and acceleration will be calculated based the values set here.
*
* calcYPR() calculate filtered value of gyro and acceleration.
* This is implementation of complementary filter.
*/

#include <Wire.h>

enum{x,y,z};
const int MPU_addr = 0x68;

//raw values from mpu
int16_t AcX, AcY, AcZ, Tmp, GyX, GyY, GyZ;

//increment of time t
float dt;
unsigned long t_prev;

//base acc, gyro values
float baseAcX, baseAcY, baseAcZ;
float baseGyX, baseGyY, baseGyZ;

//calculated sensor values
float accel_angle[3];
float gyro[3];

//filtered angle
float filtered_angle[3];

void initYRP(){
	Serial.print("initMPU()..");
	initMPU6050();
	Serial.println("getBaseAccGyro()..");
	getBaseAccGyro();
	t_prev = micros();
}
void calcYRP(){
	readMPU();

	//calc dt
	unsigned long now = micros();
	dt = (now - t_prev) / 1000000.0;
	t_prev = now;

	calcAccGyro();
	filterYRP();
}

/* functions for initiating YPR */
//establish connection w/ mpu
void initMPU6050(){
	Wire.begin();
  	Wire.beginTransmission(MPU_addr);
 	Wire.write(0x6B);
  	Wire.write(0);
  	Wire.endTransmission(true);
}
//read raw values(Ac, Gy) from mpu
void readMPU() {
	Wire.beginTransmission(MPU_addr);
	Wire.write(0x3B);
	Wire.endTransmission(false);
	Wire.requestFrom(MPU_addr, 14, true);

	AcX = Wire.read() << 8 | Wire.read();
	AcY = Wire.read() << 8 | Wire.read();
	AcZ = Wire.read() << 8 | Wire.read();
	Tmp = Wire.read() << 8 | Wire.read();
	GyX = Wire.read() << 8 | Wire.read();
	GyY = Wire.read() << 8 | Wire.read();
	GyZ = Wire.read() << 8 | Wire.read();
}
//set base values
void getBaseAccGyro(){
  	const int num_iter = 10;
  	baseAcX = baseAcY = baseAcZ = 0;
  	baseGyX = baseGyY = baseGyZ = 0;

  	for (int i = 0; i < num_iter; i++) {
   	 	readMPU();
   		baseAcX += AcX / num_iter, baseAcY += AcY / num_iter, baseAcZ += AcZ / num_iter;
    	baseGyX += GyX / num_iter, baseGyY += GyY / num_iter, baseGyZ += GyZ / num_iter;
    	delay(100);
  }
}

/* functions for initiating YPR */



/* functions for calculating YPR */
void calcAccGyro(){

	const float RADIANS_TO_DEGREES = 180 / 3.14159;
	const float GYROXYZ_TO_DEGREES_PER_SEC = 131;

	//calc acc
	float accel[3] = {AcX - baseAcX, AcY - baseAcY, AcZ + (16384 - baseAcZ)};
	float accel_xz = sqrt(pow(accel[x], 2) + pow(accel[z], 2));
	float accel_yz = sqrt(pow(accel[y], 2) + pow(accel[z], 2));

	accel_angle[x] = atan( accel[y] / accel_xz) * RADIANS_TO_DEGREES;
	accel_angle[y] = atan(-accel[x] / accel_yz) * RADIANS_TO_DEGREES;
	accel_angle[z] = 0;
	
	//calc gyro
	gyro[x] = (GyX - baseGyX) / GYROXYZ_TO_DEGREES_PER_SEC;
	gyro[y] = (GyY - baseGyY) / GYROXYZ_TO_DEGREES_PER_SEC;
	gyro[z] = (GyZ - baseGyZ) / GYROXYZ_TO_DEGREES_PER_SEC;
}

void filterYRP(){
	const float ALPHA = 0.96;

	float tmp_angle[3];
	for(int i=0; i<3; i++)
		tmp_angle[i] = filtered_angle[i] + gyro[i] * dt;

	filtered_angle[x] = ALPHA * tmp_angle[x] + (1.0 - ALPHA) * accel_angle[x];
	filtered_angle[y] = ALPHA * tmp_angle[y] + (1.0 - ALPHA) * accel_angle[y];
	filtered_angle[z] = tmp_angle[z];
}
/* functions for calculating YPR */
