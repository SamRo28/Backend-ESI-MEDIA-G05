package iso25.g05.esi_media;

import iso25.g05.esi_media.config.MongoTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {MongoTestConfig.class})
@ActiveProfiles("test")
class EsiMediaApplicationTests {

	@Test
	void contextLoads() {
	}

}
