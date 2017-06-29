package bcp.spring.angularjs.redis.dbconfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisConfiguration implements CommandLineRunner {

	public static final String PREFIXES_Z_SET = "prefixesZSet";
	
	@Value("${redis.server.host:localhost}")
	private String redisServerHost;
	
	@Value("${redis.server.port:6379 }")
	private int redisServerPort;
	
 
	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory =  new JedisConnectionFactory();
		factory.setHostName(redisServerHost);
        factory.setPort(redisServerPort);
        factory.setUsePool(true);
        
        log.info("Connecting to REDIS server {}:{}", redisServerHost, redisServerPort);
        return factory;
	}

	@Bean
	@Qualifier("StringRedisTemplate")
	StringRedisTemplate getStringRedisTemplate() {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(this.jedisConnectionFactory());
		return template;
	}

	@Bean
	@Qualifier("RedisTemplate")
	RedisTemplate<String, Object> redisTemplate() {
		
		final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(jedisConnectionFactory());
		
		// these are required to ensure keys and values are correctly serialized
		template.setKeySerializer(new StringRedisSerializer());
		// hashes are serialized as JSON containing the class name
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
		
		return template;
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
			redisTemplate().opsForZSet().add(PREFIXES_Z_SET, prefix, 0);
		});
		redisTemplate().opsForZSet().add(PREFIXES_Z_SET, line.trim() + "*", 0);
	}

	@Override
	public void run(String... args) throws Exception {
		initPrefixes();
	}
}
