#ifndef ECU_DEFINE_H_
#define ECU_DEFINE_

#define MESSEAGE_ENGINE_SPEED 0x17C
#define MESSEAGE_CVT_191 0x191
#define MESSEAGE_SRS_305 0x305
#define MESSEAGE_ODO_294 0x294

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
 
#endif /* CAN_H_ */
