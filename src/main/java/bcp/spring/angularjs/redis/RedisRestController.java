package bcp.spring.angularjs.redis;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactory;
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

	private static final String FOO = "foo";

	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;

	@Autowired
	private BeanFactory beanFactory;

	@GetMapping("singlekey/{key}")
	public RedisResult getSingleValue(@PathVariable("key") String key) {
		// String value = (String)this.template.opsForValue().get(key);

		RedisResult result = (RedisResult) this.template.opsForHash().get(FOO, key);
		return result;
	}

	@GetMapping("complete/{prefix}")
	public List<String> autoComplete(@PathVariable("prefix") String prefix) {
		EntryMatcher em = beanFactory.getBean(EntryMatcher.class, new Object[] { prefix, 20 });
		List<String> results = em.matchAll();
		return results;
	}

	@PostConstruct
	private void addValues() {
		HashOperations<String, String, RedisResult> hashOps = template.opsForHash();
		hashOps.put(FOO, "bar1", new RedisResult("baz", "whatever"));
		template.opsForSet().add("idset", "bar1");

		hashOps.put(FOO, "bar2", new RedisResult("zzz", "whatever"));
		template.opsForSet().add("idset", "bar2");

		log.info("Existing values {}", hashOps.entries(FOO));
	}

}