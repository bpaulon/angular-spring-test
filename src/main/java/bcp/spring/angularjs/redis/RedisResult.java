package bcp.spring.angularjs.redis;

import java.io.Serializable;

import lombok.Data;

@Data
public class RedisResult implements Serializable {


	private static final long serialVersionUID = 2600153054771796248L;

	private String key;
	private String value;

	public RedisResult(String key, String value) {
		this.key = key;
		this.value = value;
	}

}
