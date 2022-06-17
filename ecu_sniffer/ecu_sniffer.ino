#include <SPI.h>
#include "CanDriver_SG_ADAS.h"
#include "ECU_DEFINE.h"
#include "SaiGon_ADAS_processing.h"

struct can_frame canMsg;
MCP2515 mcp2515(10);

unsigned long previousMillis = 0;    
const long interval = 100; //100ms Interval

#define len_buffer_send 20
char send_value[len_buffer_send];

void setup() {
  Serial.begin(115200);  
  mcp2515.reset();
  mcp2515.setBitrate(CAN_500KBPS, MCP_8MHZ);
  mcp2515.setNormalMode();
  delay(100);
  Init_buffer(&send_value[0], len_buffer_send);
}

void loop() {
  if (mcp2515.readMessage(&canMsg) == MCP2515::ERROR_OK) {
    SG_ADAS_Processing(canMsg, &send_value[0], len_buffer_send); 
  }
  Task_100ms();
}
void Task_100ms()
{
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
      previousMillis = currentMillis;
      send_newframe_to_bluetooth_interval(&send_value[0], len_buffer_send); 
    }
}

void send_newframe_to_bluetooth_interval(char* p_send_array,int len)
{
  for(int i=0;i< len;i++)
  {
    Serial.print(*(p_send_array+i));
  }        
  Serial.println();
}
