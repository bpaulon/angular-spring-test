package bcp.spring.angularjs.redis;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bcp.spring.angularjs.redis.dbconfig.Movie;

@RestController
public class RedisRestController {

	@Autowired
	private BeanFactory beanFactory;

	@GetMapping("complete/{prefix}")
	public List<String> autoComplete(@PathVariable("prefix") String prefix) {
		NameMatcher em = beanFactory.getBean(NameMatcher.class, prefix, 20);
		List<String> results = em.matchAll();
		return results;
	}

	@GetMapping("search")
	public List<Movie> search(@RequestParam(value = "word", required = true) List<String> words) {
		MovieByStoryMatcher movieMatcher = beanFactory.getBean(MovieByStoryMatcher.class, words);
		return movieMatcher.match();
	}

}