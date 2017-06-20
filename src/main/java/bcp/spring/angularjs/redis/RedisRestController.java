package bcp.spring.angularjs.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bcp.spring.angularjs.redis.dbconfig.Movie;
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
	
	DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

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
	
	@GetMapping("search") 
	public List<Movie> search(@RequestParam(value="word", required=true) List<String> words) {
		
		List<Movie> movies = new ArrayList<>();
		List<String> encodedWords = words.stream()
				.map(word -> doubleMetaphone.doubleMetaphone(word, false))
				.peek(word -> log.debug("alternative encoding {}", doubleMetaphone.doubleMetaphone(word, true) ))
				.collect(Collectors.toList());
		
		log.debug("encoded words {}", encodedWords);
		// get all the movie ids by intersecting the sets
		//FIXME - out zset key should be unique
		template.opsForZSet().intersectAndStore(encodedWords.get(0), encodedWords, "out");
		
		Set<String> ids = (Set)template.opsForZSet().range("out", 0, -1);
		log.debug("ids found {}", ids);
		ids.forEach(id -> {
			Movie movie = (Movie)template.opsForHash().get("movies", Long.parseLong(id));
			log.debug("id -> {}", movie);
			movies.add(movie);
		});
		
		return movies;
	}

	@PostConstruct
	private void addValues() {
		HashOperations<String, String, RedisResult> hashOps = template.opsForHash();
		hashOps.put(FOO, "bar1", new RedisResult("baz", "whatever"));
		template.opsForSet().add("idset", "bar1");

		hashOps.put(FOO, "bar2", new RedisResult("zzz", "whatever"));
		template.opsForSet().add("idset", "bar2");

		//template.opsForValue().in
		log.info("Existing values {}", hashOps.entries(FOO));
	}

}