package bcp.spring.angularjs.redis.dbconfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MovieConfig {

	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	
	DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
	
	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;
	
	public List<Movie> readMovies() throws JsonParseException, JsonMappingException, IOException {
		List<Movie> movies = mapper.readValue(new File("movies.yaml"), new TypeReference<List<Movie>>(){}) ;
		return movies;
	}
	
	@PostConstruct
	public void initMovies() throws Exception {
		List<Movie> movies = readMovies();
		template.opsForValue().set("movie:id", 0L);
		movies.forEach(movie -> {
			// id must be incremented outside the transaction - otherwise the INCR operation
			// only gets QUEUED and NO value is returned
			long movieId = template.opsForValue().increment("movie:id", 1L);
			storeMovie(movie, movieId);
		});
	}
	
	public void storeMovie(Movie movie, long movieId) {
		template.execute(new SessionCallback<List<Object>>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public List<Object> execute(RedisOperations operations) throws DataAccessException {
				operations.multi();
				
				operations.opsForHash().put("movies", movieId, movie);
				
				Map<String, Integer> metaphones = extractWords(movie.getPlot());
				
				metaphones.keySet().forEach(mp -> {
					operations.opsForZSet().add("word:"+ mp, movieId, metaphones.get(mp));
				});
				
				return operations.exec();
			}
			
		});
	}
	
	private Map<String, Integer> extractWords(String input) {
		Map<String, Integer> words = new HashMap<>();
		Pattern p = Pattern.compile("[\\w']+");
		Matcher m = p.matcher(input);

		while (m.find()) {
			String word = input.substring(m.start(), m.end());
			String encoding = doubleMetaphone.doubleMetaphone(word, false);
			Integer frequency = words.getOrDefault(encoding, 0);
			words.put(encoding, ++frequency);

			// optional - do the same for the alternate metaphone encoding
		}

		log.debug("found in input: {}", words);
		return words;
	}
}
