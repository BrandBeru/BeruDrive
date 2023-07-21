package org.beru.drive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DriveApplicationTests {
	@Test
	void contextLoads() {

		int max = 2172;
		int min = 212;

		double abs = Math.abs(min-max);
		float average = (float) (max + min) /2;

		double p = average/abs;
		System.out.println(p);
	}

}
