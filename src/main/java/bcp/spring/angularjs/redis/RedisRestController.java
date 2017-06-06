package bcp.spring.angularjs.redis;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
public class RedisRestController {

	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;

	@GetMapping("singlekey/{key}")
	public RedisResult getSingleValue(@PathVariable("key") String key){
		//String value = (String)this.template.opsForValue().get(key);

		RedisResult result = (RedisResult)this.template.opsForHash().get("foo", key);
		return result;
	}
	
	@PostConstruct
	private void addValues() {
		HashOperations<String, String, RedisResult> hashOps = template.opsForHash();
		hashOps.put("foo", "bar", new RedisResult("baz", "whatever"));
		
		log.info("added values");
	}
	
}