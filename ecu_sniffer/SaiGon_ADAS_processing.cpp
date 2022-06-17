/*
 *      Author: Thinh
 */
#include "ECU_DEFINE.h"
#include "SaiGon_ADAS_processing.h"
#include "stdio.h"

/*
 * SEND BUFFER PARAMETER STRUCT PROTOCOL
 * Start frame: '!'
 * BYTE1(Ascii) Warning Door : 0 (False) or 1(True)
 * BYTE2(Ascii) Warning Brake : 0 (False) or 1(True)
 * BYTE3(Ascii) Warning Seat Belt : 0 (False) or 1(True)
 * BYTE4(Ascii) Vehicle Speed
 * BYTE5(Ascii) Vehicle Speed
 * BYTE6(Ascii) Vehicle Speed
 * BYTE7(Ascii) Engine Speed
 * BYTE8(Ascii) Engine Speed
 * BYTE9(Ascii) Engine Speed
 * BYTE10(Ascii) Engine Speed
 * BYTE11(Ascii) Engine Speed
 * BYTE12(Ascii) GEAR(0-P,1-N,2-D,3-R,4-S,5-L)
 * BYTE13(Ascii) ODO Meter
 * BYTE14(Ascii) ODO Meter
 * BYTE15(Ascii) ODO Meter
 * BYTE16(Ascii) ODO Meter
 * BYTE17(Ascii) ODO Meter
 * BYTE18(Ascii) ODO Meter
 * Stop frame: '@'
 * Confirm End Frame: \n
 */

void SG_ADAS_Processing(can_frame Can_Msg, char* Send_buffer,int len)
{  
   if(Can_Msg.can_id == MESSEAGE_ENGINE_SPEED) //check engine speed message
   {
     //Check CAN_MATRIX to get correct position of byte
     uint16_t t_EngineSpeed= Can_Msg.data[2]<<8 | Can_Msg.data[3];
     char buf[5];
     // convert int to string array
     sprintf(buf, "%05d", t_EngineSpeed);
     //update value to buffer array frame send via bluetooth 
     for (int i = 0; i < 5; i++) {
         *(Send_buffer+7+i)= buf[i];
     }
   }
   else if (Can_Msg.can_id == MESSEAGE_CVT_191)//check GEAR message
   {
     uint8_t t_buf = Can_Msg.data[0] & 0x3F;
     switch (t_buf){
         case 1:// 0x01 - P
           // statements
           *(Send_buffer+12)='0';
           break;
         case 2:// 0x02 - R
           *(Send_buffer+12)='3';
           // statements
           break;    
         case 4: // 0x04 - N
           *(Send_buffer+12)='1';
           // statements 
             break;                
         case 8:// 0x08 - D
           *(Send_buffer+12)='2';
           // statements
           break;       
         case 16: // 0x10 - S
           *(Send_buffer+12)='4';
           // statements
           break;  
         case 32: // 0x20 - L
           *(Send_buffer+12)='5';
           // statements
           break;                          
         default: //invalid Gear
           *(Send_buffer+12)='9';
           // default statements
     }
   }
   else if(Can_Msg.can_id ==MESSEAGE_SRS_305) //Get seatBetl Status
   {
     uint8_t buf = Can_Msg.data[1];
     buf = buf & 0x30;
     if(buf != 0)
     {
       *(Send_buffer+3)='1';
     }
     else
     {
       *(Send_buffer+3)='0';
     }
   }
   else if(Can_Msg.can_id == MESSEAGE_ODO_294) //check engine speed message
   {
     //Check CAN_MATRIX to get correct position of byte
     uint32_t t_odometer = (Can_Msg.data[3]<<16) | (Can_Msg.data[4]<<8) | Can_Msg.data[5];
     char buf[6];
     // convert int to string array
     sprintf(buf, "%06d", t_odometer);
     //update value to buffer array frame send via bluetooth 
     for (int i = 0; i < 6; i++) {
         *(Send_buffer+13+i)= buf[i];
     }
   }
}
    // if( 
    //     (canMsg.can_id == MESSEAGE_ENG_13C) || 
    //     (canMsg.can_id == MESSEAGE_ENG_17C) || 
    //     (canMsg.can_id == MESSEAGE_CVT_191)
    //    )
    // {
    //   Serial.print(canMsg.can_id, HEX); // print ID
    //   Serial.print("-"); 
    //   Serial.print(canMsg.can_dlc, HEX); // print DLC
    //   Serial.print("-");
      
    //   for (int i = 0; i<canMsg.can_dlc; i++)  {  // print the data
    //     Serial.print(canMsg.data[i],HEX);
    //     Serial.print(" ");
    //   }
    //   Serial.println();  
    // }   x

void Init_buffer(char* Send_buffer,int len)
{
  *Send_buffer='!';
  *(Send_buffer+len-1)='@';
  for(int i=1;i< len-1;i++)
  {
    *(Send_buffer+i)='0';
  }
}

//  void SG_ADAS_Processing(can_frame Can_Msg, char* Send_buffer,int len)
// {  
//       *(Send_buffer+1) = random(0,2)+0x30;
//       *(Send_buffer+2) = random(0,2)+0x30;
//       *(Send_buffer+3) = random(0,2)+0x30;
//       //Check CAN_MATRIX to get correct position of byte
//       uint16_t t_EngineSpeed= random(0,500);
//       char buf[5];
//       // convert int to string array
//       sprintf(buf, "%05d", t_EngineSpeed);
//       //update value to buffer array frame send via bluetooth 
//       for (int i = 0; i < 5; i++) {
//           *(Send_buffer+7+i)= buf[i];
//       } 

//       uint8_t t_buf = random(0,30) & 0x3F;
//       switch (t_buf){
//           case 1:// 0x01 - P
//             // statements
//             *(Send_buffer+12)='0';
//             break;
//           case 2:// 0x02 - R
//             *(Send_buffer+12)='3';
//             // statements
//             break;    
//           case 4: // 0x04 - N
//             *(Send_buffer+12)='1';
//             // statements 
//               break;                
//           case 8:// 0x08 - D
//             *(Send_buffer+12)='2';
//             // statements
//             break;       
//           case 16: // 0x10 - S
//             *(Send_buffer+12)='4';
//             // statements
//             break;  
//           case 32: // 0x20 - L
//             *(Send_buffer+12)='5';
//             // statements
//             break;                          
//           default: //invalid Gear
//             *(Send_buffer+12)='9';
//             // default statements
//       }

//       uint8_t buf2 = random(16,19);
//       buf2 = buf2 & 0x30;
//       if(buf2 != 0)
//       {
//         *(Send_buffer+3)='1';
//       }
//       else
//       {
//         *(Send_buffer+3)='0';
//       }
  
//       //Check CAN_MATRIX to get correct position of byte
//       uint32_t t_odometer = random(0,300000);
//       char buf3[6];
//       // convert int to string array
//       sprintf(buf3, "%06d", t_odometer);
//       //update value to buffer array frame send via bluetooth 
//       for (int i = 0; i < 6; i++) {
//           *(Send_buffer+13+i)= buf[i];
//       }
// }    
