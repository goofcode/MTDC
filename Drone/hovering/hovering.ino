/*
* Author: Mookeun Ji, goofcode@gmail.com
* 
* This source contains 2 parts of main function, setup() and loop().
*
* setup() establishes Serial connection, initiate YPR, PID and motors.
* This setup() will run only once to set up all the variables to be initialized before hovering.
*
* loop() is the main loop for drone, which calculate YPR, PID and motor speed and update motor speed repeatedly. 
* This loop() will be end after specified time elapsed and this time can be adjusted by changing 'run_time'
* 
*/


#define __SERIAL_LOG__
//extern for serial print
extern float dt;
extern float filtered_angle[3], output[3];
extern float motor_speed[4];


const int delay_time = 3;   //sec
const unsigned long run_time = 300;  //sec


unsigned long start_time;
extern float throttle = 100;

void setup() {

	Serial.begin(115200);

	Serial.print("initiating YRP...");
	initYRP();
	Serial.println("initiating PID...");
	initPID();
	Serial.println("initiating Motors...");
	initMotor();  

	Serial.print("\ndelaying");
	for(int i=delay_time; i>0; i--){
		Serial.print("...");
		Serial.print(i);
		delay(1000);
	}

	start_time = millis();

}

void loop() {

	calcYRP();
	calcPID();
	calcMotorSpeed();

	updateMotorSpeed();

  
	if(millis() % 5 == 0 ) showStatus();
	if((millis() - start_time) > run_time * 1000) exit(0);
}

void showStatus() {
	Serial.print(F("DEL:")); Serial.print(dt, DEC); Serial.print("\t");
	Serial.print(F("#RPY:")); Serial.print(filtered_angle[1], 2); Serial.print(F(",\t")); Serial.print(filtered_angle[0], 2);Serial.print(F(",\t"));Serial.print(filtered_angle[2], 2); Serial.print("\t\t");
	Serial.print(F("#PID:")); Serial.print(output[1], 2); Serial.print(F(",\t")); Serial.print(output[0], 2); Serial.print(F(",\t")); Serial.print(output[2], 2); Serial.print("\t\t");
	Serial.print(F("#A:")); Serial.print(motor_speed[0], 2); Serial.print(F("\t#B:")); Serial.print(motor_speed[1], 2);
	Serial.print(F("\t#C:")); Serial.print(motor_speed[2], 2); Serial.print(F("\t#D:")); Serial.println(motor_speed[3], 2);
}

void serialEvent()
{
  while (Serial.available()) {
    throttle = Serial.parseFloat();
  }
}
