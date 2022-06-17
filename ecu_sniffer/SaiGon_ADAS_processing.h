/*
 * CanHacker.h
 *
 *      Author: TTNHC
 */

#ifndef SAIGON_ADAS_PROCESSING_H_
#define SAIGON_ADAS_PROCESSING_H_

#include "can.h"
#include "CanDriver_SG_ADAS.h"

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
 
void Init_buffer(char* Send_buffer,int len);
void SG_ADAS_Processing(can_frame Can_Msg, char* Send_buffer,int len);

#endif /* CANHACKER_H_ */
