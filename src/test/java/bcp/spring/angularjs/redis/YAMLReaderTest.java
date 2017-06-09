package bcp.spring.angularjs.redis;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YAMLReaderTest {

	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	
	@Test
	public void testWrite() throws JsonProcessingException {
		 ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		 ArrayList<Movie> movieList = new ArrayList<>();
		 movieList.add(new Movie("Alien Ressurection", "more aliens", "Ridley Scott", LocalDate.of(1980, 2, 1)));
		 movieList.add(new Movie("Alien Covenant", "evan more aliens", "Ridley Scott", LocalDate.of(2017, 2, 1)));
		 String movieY = mapper.writeValueAsString(movieList);
		 System.out.println(movieY);
		 
	}
	
	@Test
	public void testRead() throws JsonParseException, JsonMappingException, IOException {
		//List<Movie> movie = mapper.readValue(new File("movies.yaml").g,new TypeReference<List<Movie>>());
		List<Movie> movies = mapper.readValue(new File("movies.yaml"), new TypeReference<List<Movie>>(){}) ;
		System.out.println(movies);
	}
	
}
