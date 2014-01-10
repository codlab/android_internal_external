package eu.codlab.int2ext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class SwitchAtBootReceiver extends BroadcastReceiver {

    private static class SwitchAtBoot extends AsyncTask<String, String, String>{
        private final Context _context;

        SwitchAtBoot(Context context){
            _context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d("SwitchAtBootReceiver","start");
            CopyProgram c = new CopyProgram(_context);
            Log.d("SwitchAtBootReceiver","finish");
            try{
                Thread.sleep(500);
            }catch(Exception e){

            }
            if(CopyProgram.existsProgramBootReceiver())
                c.copyProgramBoot();

            return null;
        }
    }

	@Override
	public void onReceive(Context arg0, Intent arg1) {
        SwitchAtBoot b = new SwitchAtBoot(arg0);
        b.execute(new String[]{""});

	}

}
