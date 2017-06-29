package bcp.spring.angularjs.redis;

import static bcp.spring.angularjs.redis.dbconfig.RedisConfiguration.PREFIXES_Z_SET;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "prototype")
@Slf4j
public class EntryMatcher {

	private String prefix;
	private int count;

	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;

	public EntryMatcher(String prefix, int count) {
		this.prefix = prefix;
		this.count = count;
	}

	/**
	 * Match all the entries in the REDIS set that are whole words (i.e. end
	 * with '*') and start with prefix
	 * 
	 * @return list of entries
	 */
	public List<String> matchAll() {
		Long startIndex = template.opsForZSet().rank(PREFIXES_Z_SET, prefix);
		if (startIndex == null) {
			return new ArrayList<>();
		} else {
			return processFromIndex(startIndex);
		}
	}

	/**
	 * Process entries in the ordered set (ZSet) and return the matches
	 * 
	 * @param startIndex
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> processFromIndex(Long startIndex) {
		List<String> results = new ArrayList<>(count);

		final long rangelen = 50; // This is not random, try to get replies < MTU size

		boolean moreMatches = true;
		while (moreMatches && results.size() != count) {
			Set<String> range = (Set) template.opsForZSet().range(PREFIXES_Z_SET, startIndex,
					startIndex + rangelen - 1);
			log.debug("Processing range: {}", range);
			startIndex += rangelen;
			
			if (range == null || range.size() == 0) {
				return results;
			}

			Predicate<String> matchPredicate = (String entry) -> {
				int min = Math.min(entry.length(), prefix.length());
				return entry.substring(0, min).equals(prefix.substring(0, min));
			};

			// if all the entries in the range match the prefix there are more
			// matches in the next range
			moreMatches = range.stream().allMatch(matchPredicate);

			List<String> matchingEntries = StreamUtils.takeWhile(range.stream(), matchPredicate)
					.filter(entry -> entry.endsWith("*"))
					.limit(count - results.size())
					.map(entry -> entry.substring(0, entry.lastIndexOf("*")))
					.collect(Collectors.toList());

			results.addAll(matchingEntries);
			log.debug("Added matching entries:{}", matchingEntries);
		}
		
		return results;
	}
}
