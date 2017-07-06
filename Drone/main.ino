#include <Servo.h>
Servo esc1; //아두이노로 컨트롤하는 bldc모터의 경우
Servo esc2; //아두이노로 컨트롤하는 bldc모터의 경우
Servo esc3; //아두이노로 컨트롤하는 bldc모터의 경우
Servo esc4; //아두이노로 컨트롤하는 bldc모터의 경우

int m[4] = {0,0,0,0};

void setup() {

  Serial.begin(9600); //시리얼 통신으로 조작한다.

  esc1.attach(2, 1000, 2000); //나는 6번, 7번에 연결했다.
  esc2.attach(3, 1000, 2000); //나는 6번, 7번에 연결했다.
  esc3.attach(4, 1000, 2000); //나는 6번, 7번에 연결했다.
  esc4.attach(5, 1000, 2000); //나는 6번, 7번에 연결했다.
  
  Serial.setTimeout(50); //아래 parseInt로 값을 받는게 있는데, 이게 기본 딜레이가 1초가 있다. 그걸 50ms로 설정해주는 것. 속도를 빠르게 하기 위함.

  esc1.write(0); //초기값은 무조건 0!!! 캘리브레이션과 다르다.
  esc2.write(0); //초기값은 무조건 0!!! 캘리브레이션과 다르다.
  esc3.write(0); //초기값은 무조건 0!!! 캘리브레이션과 다르다.
  esc4.write(0); //초기값은 무조건 0!!! 캘리브레이션과 다르다.

}

void loop() {

  esc1.write(m[0]); //시리얼 통신으로 받은 값을 모터에 넣어주자!
  esc2.write(m[1]); //시리얼 통신으로 받은 값을 모터에 넣어주자!
  esc3.write(m[2]); //시리얼 통신으로 받은 값을 모터에 넣어주자!
  esc4.write(m[3]); //시리얼 통신으로 받은 값을 모터에 넣어주자!
 
  Serial.print(m[0]); Serial.print("  ");
  Serial.print(m[1]); Serial.print("  ");
  Serial.print(m[2]); Serial.print("  ");
  Serial.print(m[3]);
  Serial.println('\t');
}


void serialEvent()
{
  while (Serial.available()) {
      m[0] = Serial.parseInt();
      m[1] = Serial.parseInt();
      m[2] = Serial.parseInt();
      m[3] = Serial.parseInt();
  }
}
