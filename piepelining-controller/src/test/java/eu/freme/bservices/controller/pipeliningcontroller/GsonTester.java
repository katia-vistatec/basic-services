/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.bservices.controller.pipeliningcontroller;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * <p>Copyright 2015 MMLab, UGent</p>
 *
 * @author Gerald Haesendonck
 */
public class GsonTester {
	private final Gson gson = new Gson();

	private class AClass {
		private String aString;
		private int anInt;
		private List<Integer> anIntList;

		public AClass() {
			Random r = new Random();
			long l = r.nextLong();
			aString = Long.toBinaryString(l);
			anInt = r.nextInt();
			anIntList = new ArrayList<>();

			for (int i = 0; i < 10; i++) {
				anIntList.add(r.nextInt());
			}
		}

		public String getaString() {
			return aString;
		}

		public int getAnInt() {
			return anInt;
		}

		public List<Integer> getAnIntList() {
			return anIntList;
		}
	}

	@Test
	public void testMultiThreading() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(1000);

		for (int i = 0; i < 1000000; i++) {
			executor.submit(() -> {
				AClass aClass = new AClass();
				int anInt = aClass.getAnInt();
				String aString = aClass.getaString();
				List<Integer> anIntList = aClass.getAnIntList();

				// now serialize and deserialize
				String json = gson.toJson(aClass);
				AClass bClass = gson.fromJson(json, AClass.class);

				assertEquals(anInt, bClass.getAnInt());
				assertEquals(aString, bClass.getaString());
				assertEquals(anIntList, bClass.getAnIntList());
			});
		}
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.MINUTES);
	}
}
