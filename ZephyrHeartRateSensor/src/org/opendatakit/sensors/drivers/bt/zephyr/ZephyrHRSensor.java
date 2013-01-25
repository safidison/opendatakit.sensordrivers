/*
 * Copyright (C) 2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.sensors.drivers.bt.zephyr;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.sensors.SensorDataPacket;
import org.opendatakit.sensors.SensorDataParseResponse;
import org.opendatakit.sensors.SensorParameter;
import org.opendatakit.sensors.drivers.AbstractDriverBaseV2;

import android.os.Bundle;
import android.util.Log;

public class ZephyrHRSensor extends AbstractDriverBaseV2  {

	private static final String BEAT_COUNT = "BC";
	private static final String HEART_RATE = "HR";
	public static final String TAG = "ZephyrHRSensorV2";

	public ZephyrHRSensor() {
		super();
		
		// data reporting parameters
		sensorParams.add(new SensorParameter(HEART_RATE, SensorParameter.Type.INTEGER, SensorParameter.Purpose.DATA, "Heart Rate"));
		sensorParams.add(new SensorParameter(BEAT_COUNT, SensorParameter.Type.INTEGER, SensorParameter.Purpose.DATA, "Beat counter that rolls over"));
		Log.d(TAG," constructed" );
	}

	@Override
	public SensorDataParseResponse getSensorData(long maxNumReadings, List<SensorDataPacket> rawData, byte[] remainingData) {
		List<Bundle> allData = new ArrayList<Bundle>();		
		Log.d(TAG," sensor driver get dataV2. sdp list sz: " + rawData.size());
		List<Byte> dataBuffer = new ArrayList<Byte>();
		
		// Copy over the remaining bytes
		if(remainingData != null) {
			for (Byte b : remainingData) {
				dataBuffer.add(b);
			}
		}
		// Add the new raw data
		for(SensorDataPacket pkt: rawData) {
			byte [] payload = pkt.getPayload();
			Log.d(TAG, " sdp length: " + payload.length);

			for (int i = 0; i < payload.length; i++) {
				dataBuffer.add(payload[i]);
			}			
		}

		// Parse all data into packet sizes of 60 bytes
		int masked;
		while (dataBuffer.size() >= 60) {
			Log.d(TAG,"dataBuffer size: " + dataBuffer.size());	
			Bundle parsedPkt = new Bundle();
			allData.add(parsedPkt);

			for(int i = 0; i < 60; i++) {	
				byte b = dataBuffer.remove(0);	

				if(i == 12) {
					masked = b & 0xff;					
					parsedPkt.putInt(HEART_RATE, masked);
					Log.d(TAG,"V2 HR: " + masked);
				}
				else if(i == 13) {
					masked = b & 0xff;
					parsedPkt.putInt(BEAT_COUNT, masked);
					Log.d(TAG,"V2 BC: " + masked);
				}
			}
		}

		// Copy data back into remaining buffer
		byte[] newRemainingData = new byte[dataBuffer.size()];
		for (int i = 0; i < dataBuffer.size(); i++) {
			newRemainingData[i] = dataBuffer.get(i);
		}
		
		Log.d(TAG,"all done dataBuffer size: " + dataBuffer.size());		
		return new SensorDataParseResponse(allData, newRemainingData);	
	}

	@Override
	public byte[] configureCmd(String setting, Bundle params) {

		if (setting.equals("TEST")) {
			String message = "Z\n";
			return message.getBytes();
		}

		return "".getBytes();
	}
	
}
