package bcp.spring.angularjs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.experimental.Accessors;

@Accessors(fluent=true)
@lombok.Getter
@lombok.Setter
@lombok.ToString
public class User {

	@JsonProperty("id")
	private String id;
	
	@JsonProperty("firstName")
	private String firstName;
	
	@JsonProperty("secondName")
	private String secondName;
	
}
