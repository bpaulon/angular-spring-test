package bcp.spring.angularjs.redis;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
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
		RedisResult result = (RedisResult) this.template.opsForHash().get(FOO, key);
		return result;
	}

	
	@GetMapping("complete/{prefix}")
	public List<String> autoComplete(@PathVariable("prefix") String prefix) {
		EntryMatcher em = beanFactory.getBean(EntryMatcher.class, prefix, 20);
		List<String> results = em.matchAll();
		return results;
	}
	
	
	@GetMapping("search") 
	public List<Movie> search(@RequestParam(value="word", required=true) List<String> words) {
		
		List<String> searchKeys = words.stream()
				.filter(StreamUtils.stringNotNullOrEmpty())
				.map(this::createKeyForWord)
				.collect(toList());
		log.debug("Search keys: {}", searchKeys);
		
		// get all the movie ids by intersecting the sets
		//FIXME - the key of the ZSET in output should be unique
		template.opsForZSet().intersectAndStore(searchKeys.get(0), searchKeys, "out");
		template.expire("out", 10, TimeUnit.SECONDS);
		
		@SuppressWarnings({"unchecked","rawtypes"})
		Set<TypedTuple<String>> ids = (Set)template.opsForZSet().rangeWithScores("out", 0, -1);
		log.debug("ids found {}", ids);
		
		
		List<Movie> movies = ids.stream()
			// each movie id has an associated score which is the frequency of the searched words
			// sort the ids in the reverse order so the first movie id will be the one with the
			// highest frequency of the searched words
			.sorted(Collections.reverseOrder(Comparator.comparing(TypedTuple::getScore)))
			// map id to movie
			.peek(i -> log.debug("mapping id {} >>", i.getValue()))
			.map(this::getMovieById)
			.peek(m -> log.debug(">> {}", m))
			.collect(toList());
				
		return movies;
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
	
	private String createKeyForWord(String word) {
		return "word:" + doubleMetaphone.doubleMetaphone(word, false);
	}
	
	private Movie getMovieById(TypedTuple<String> idWithScore) {
		long movieId = Long.parseLong(idWithScore.getValue());
		return (Movie)template.opsForHash().get("movies", movieId);
	}

}