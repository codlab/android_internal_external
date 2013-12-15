package eu.codlab.int2ext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Environment;

/**
 * This class downloads the json from the website to the sdcard to the internal/external sdcard
 * /sdcard/int2ext_roms
 * 
 * @author kevin le perf
 * @version 1.0
 *
 */
public class AsyncDownloadJSON extends AsyncTask<URL, Integer, Long>{
	private Prefs _parent;

	private AsyncDownloadJSON(){
		_parent = null;
	}

	public AsyncDownloadJSON(Prefs parent){
		this();
		_parent = parent;
	}

	@Override
	protected Long doInBackground(URL... arg0) {
		if(arg0 != null && arg0.length > 0){
			try {
				HttpURLConnection urlConnection = (HttpURLConnection) arg0[0].openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				urlConnection.connect();

				File sdcard = Environment.getExternalStorageDirectory();
				File file = new File(sdcard, "int2ext_roms");
				file.createNewFile();
				
				FileOutputStream fileOutput = new FileOutputStream(file);
				InputStream inputStream = urlConnection.getInputStream();

				byte[] buffer = new byte[1024];
				int bufferLength = 0;

				while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
					fileOutput.write(buffer, 0, bufferLength);
				}
				fileOutput.close();
				
				CopyProgram c = new CopyProgram(_parent.getActivity());
				if(c.saveJSON(file.getAbsolutePath())){
					_parent.jsonDownloadOk();
				}else{
					_parent.jsonDownloadFailure();
				}

			} catch (Exception e) {
				e.printStackTrace();
				_parent.jsonDownloadFailure();
			}
		}
		return null;
	}

}
