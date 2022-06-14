#include "BluetoothSerial.h"

/*
  Code support cho module ESP32.
  Dieu Chinh chan led theo dung module su dung de kiem tra trap/exception
*/
#define LED 23

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

bool ledState = false;  
unsigned long previousMillis =0;
void setup() {
  Serial.begin(115200);
  Serial2.begin(115200);
  SerialBT.begin("VehicleDemo"); //Bluetooth device name
  pinMode(LED, OUTPUT);
  delay(50);
}
void loop() {
  //read from serial 2 and foward to bluetooth 
  if (Serial2.available()) {
    SerialBT.write(Serial2.read());
  }
  //read from bluetooth and foward to serial 2 
  if (SerialBT.available()) {
    Serial2.write(SerialBT.read());
  }
  blink_led();
}

//puspose of this function to check trap/exeption
void blink_led()
{
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= 250) {
    // save the last time you blinked the LED
    previousMillis = currentMillis;
    ledState = !ledState;
    // set the LED with the ledState of the variable:
    digitalWrite(LED, ledState);
  }
}
