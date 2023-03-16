package green.dividendfinder.config;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean  // 싱글톤으로 관리하기 위해 Bean 등록
    public Trie<String, String> trie() {
        return new PatriciaTrie<>();
    }

}
