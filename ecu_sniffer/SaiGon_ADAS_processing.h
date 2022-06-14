/*
 * CanHacker.h
 *
 *      Author: TTNHC
 */

#ifndef SAIGON_ADAS_PROCESSING_H_
#define SAIGON_ADAS_PROCESSING_H_

#include "can.h"
#include "CanDriver_SG_ADAS.h"


void Init_buffer(char* Send_buffer,int len);
void SG_ADAS_Processing(can_frame Can_Msg, char* Send_buffer,int len);

#endif /* CANHACKER_H_ */
