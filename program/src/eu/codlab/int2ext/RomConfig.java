package eu.codlab.int2ext;

public class RomConfig {
	private String _id;
	private String _name;
	private String _summary;
	private String _internal;
	private String _external;
	private String _device;
	
	/**
	 * 
	 * @param name
	 * @param summary
	 * @param id
	 * @param internal
	 * @param external
	 * @param device
	 */
	public RomConfig(String name, String summary, String id, String internal,
			String external, String device){
		_id = id;
		_name = name;
		_summary = summary;
		_internal = internal;
		_external = external;
		_device = device;
	}
	
	public String getId(){
		return _id;
	}
	
	public String getName(){
		return _name;
	}
	public String getSummary(){
		return _summary;
	}
	public String getInternal(){
		return _internal;
	}
	public String getExternal(){
		return _external;
	}
	public String getDevice(){
		return _device;
	}
}
