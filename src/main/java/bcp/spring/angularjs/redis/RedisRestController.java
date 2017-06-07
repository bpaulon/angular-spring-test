package bcp.spring.angularjs.redis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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

	private static final String FOO = "foo";

	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;

	@GetMapping("singlekey/{key}")
	public RedisResult getSingleValue(@PathVariable("key") String key) {
		// String value = (String)this.template.opsForValue().get(key);

		RedisResult result = (RedisResult) this.template.opsForHash().get(FOO, key);
		return result;
	}

	@GetMapping("complete/{prefix}")
	public List<String> autoComplete(@PathVariable("prefix") String prefix) {

		List<String> results = complete(prefix, 20);
		return results;
	}

	@PostConstruct
	private void addValues() {
		HashOperations<String, String, RedisResult> hashOps = template.opsForHash();
		hashOps.put(FOO, "bar1", new RedisResult("baz", "whatever"));
		template.opsForSet().add("idset", "bar1");

		hashOps.put(FOO, "bar2", new RedisResult("zzz", "whatever"));
		template.opsForSet().add("idset", "bar2");

		initPrefixes();
		log.info("Existing values {}", hashOps.entries(FOO));
	}

	private List<String> complete(String prefix, int count) {
		List<String> results = new ArrayList<>(count);
		Long startIndex = template.opsForZSet().rank("prefixesZSet", prefix);
		if (startIndex == null) {
			return results;
		}

		final long rangelen = 50; // This is not random, try to get replies <  MTU size

		

		while (results.size() != count) {
			
			Set<Object> range = template.opsForZSet().range("prefixesZSet", startIndex, startIndex + rangelen - 1);
			log.info("range: {}", range);
			
			startIndex += rangelen;
			if (range == null || range.size() == 0) {
				return results;
			}
			
			for (Object obj : range) {
				String entry = (String) obj;
				//log.debug("processing > {}", entry);

				int min = Math.min(entry.length(), prefix.length());
				if (!entry.substring(0, min).equals(prefix.substring(0, min))) {
					//log.debug("{} != {}", entry.substring(0, min), prefix.substring(0, min));
					count = results.size();
					break;
				}
				if (entry.endsWith("*") && results.size() != count) {
					results.add(entry.substring(0, entry.lastIndexOf("*")));
				}

			}
			;
		}
		return results;
	}

	private void initPrefixes() {
		File inputFile = new File("female-names.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				processLine(line);
			}
		} catch (IOException ioe) {
			log.error("could not create prefixes", ioe);
		}
	}

	private void processLine(String line) {
		final String entry = line.trim();
		IntStream.range(1, entry.length()).forEach(n -> {
			String prefix = entry.substring(0, n);
			template.opsForZSet().add("prefixesZSet", prefix, 0);
		});
		template.opsForZSet().add("prefixesZSet", line.trim() + "*", 0);
	}

}