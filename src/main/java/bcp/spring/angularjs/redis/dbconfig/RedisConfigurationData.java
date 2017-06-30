package bcp.spring.angularjs.redis.dbconfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisConfigurationData implements CommandLineRunner {

	public static final String PREFIXES_Z_SET = "prefixesZSet";

	private File dataFile;

	@SuppressWarnings("rawtypes")
	private RedisTemplate redisTemplate;

	@Autowired
	public RedisConfigurationData(@Value("classpath:/config/female-names.txt") Resource file,
			@SuppressWarnings("rawtypes") @Qualifier("RedisTemplate") RedisTemplate redisTemplate) throws IOException {
		dataFile = file.getFile();
		this.redisTemplate = redisTemplate;
	}

	private void initPrefixes() {
		try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				processLine(line);
			}
		} catch (IOException ioe) {
			log.error("could not create prefixes", ioe);
		}
	}

	@SuppressWarnings("unchecked")
	private void processLine(String line) {
		final String entry = line.trim();
		IntStream.range(1, entry.length()).forEach(n -> {
			String prefix = entry.substring(0, n);
			redisTemplate.opsForZSet().add(PREFIXES_Z_SET, prefix, 0);
		});
		redisTemplate.opsForZSet().add(PREFIXES_Z_SET, line.trim() + "*", 0);
	}

	@Override
	public void run(String... args) throws Exception {
		initPrefixes();
	}
}