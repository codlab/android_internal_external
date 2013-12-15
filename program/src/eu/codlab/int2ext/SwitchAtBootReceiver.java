package eu.codlab.int2ext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SwitchAtBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.d("SwitchAtBootReceiver","start");
		CopyProgram c = new CopyProgram(arg0);
		Log.d("SwitchAtBootReceiver","finish");
		try{
			Thread.sleep(5000);
		}catch(Exception e){
			
		}
		if(CopyProgram.existsProgramBootReceiver())
			c.copyProgramBoot();
		
	}

}
