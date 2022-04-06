/*
 * Copyright (c) 2017, Andreas Fagschlunger. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "cim/at_o2xfs_xfs_v3_20_cim_PositionCapability320Test.h"

#include <Windows.h>
#include <XFSCIM.H>
#include "at.o2xfs.win32.h"

static WFSCIMPOSCAPS PosCapabilities;
static LPSTR lpszExtra = "P6=2\0";

JNIEXPORT jobject JNICALL Java_at_o2xfs_xfs_v3_120_cim_PositionCapability320Test_buildPositionCapability320(JNIEnv *env, jobject obj) {
	PosCapabilities.fwPosition = WFS_CIM_POSOUTFRONT;
	PosCapabilities.fwUsage = WFS_CIM_POSREFUSE | WFS_CIM_POSROLLBACK;
	PosCapabilities.bShutterControl = FALSE;
	PosCapabilities.bItemsTakenSensor = TRUE;
	PosCapabilities.bItemsInsertedSensor = TRUE;
	PosCapabilities.fwRetractAreas = WFS_CIM_RA_RETRACT | WFS_CIM_RA_TRANSPORT | WFS_CIM_RA_STACKER;
	PosCapabilities.lpszExtra = lpszExtra;
	PosCapabilities.bPresentControl = TRUE;
	return NewBuffer(env, &PosCapabilities, sizeof(WFSCIMPOSCAPS));
}
