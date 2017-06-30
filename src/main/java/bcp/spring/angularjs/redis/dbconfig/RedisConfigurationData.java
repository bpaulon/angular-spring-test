package bcp.spring.angularjs.redis.dbconfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import bcp.spring.angularjs.redis.StreamUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisConfigurationData implements CommandLineRunner {

	public static final String PREFIXES_Z_SET = "prefixesZSet";

	private File dataFile;

	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public RedisConfigurationData(@Value("classpath:/config/female-names.txt") Resource file,
			@Qualifier("RedisTemplate") RedisTemplate<String, Object> redisTemplate) throws IOException {
		dataFile = file.getFile();
		this.redisTemplate = redisTemplate;
	}

	
	/**
	 * Runs last after all the components are initialized
	 */
	@Override
	public void run(String... args) throws Exception {
		initPrefixes();
	}
	
	/**
	 * Decomposes the names in the file and creates a list of prefixes
	 */
	private void initPrefixes() throws IOException {
		Path path = Paths.get(dataFile.getPath());
		try {
			processFile(path);
		} catch (IOException e) {
			log.error("Could not processs path {}", path, e);
			throw e;
		}
	}

	/**
	 * Processes the file filtering the empty lines
	 * 
	 * @param path the path of the file
	 * @throws IOException
	 */
	public void processFile(Path path) throws IOException {
		Files.lines(path)
			.filter(StreamUtils.stringNotNullOrEmpty())
			.forEach(this::processLine);
	}
	
	/**
	 * Writes all the prefixes including the full name ending in "*"
	 * 
	 * @param line
	 */
	public void processLine(String line) {
		final String l = line.trim();
		
		List<String> values = IntStream.range(1, l.length())
			.mapToObj(n -> l.substring(0, n))
			.collect(Collectors.toList());
		
		values.add(l + "*");
		values.forEach(v -> redisTemplate.opsForZSet().add(PREFIXES_Z_SET, v, 0));
	}

}