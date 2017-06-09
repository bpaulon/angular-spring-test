package bcp.spring.angularjs.redis;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.junit.Test;

public class TestDoubleMetaphone {

	DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

	private void print(String value) {
		System.out.println(doubleMetaphone.doubleMetaphone(value, true));
		System.out.println(doubleMetaphone.encode(value));
	}

	@Test
	public void test() {

		// print("Adamowicz");
		print("exceptions");
		print("exceptin");
		print("python");
		print("pyhton");

	}

}
