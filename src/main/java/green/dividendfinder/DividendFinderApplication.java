package green.dividendfinder;

import green.dividendfinder.model.Company;
import green.dividendfinder.model.Dividend;
import green.dividendfinder.model.ScrapedResult;
import green.dividendfinder.scraper.Scraper;
import green.dividendfinder.scraper.YahooFinanceScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class DividendFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DividendFinderApplication.class, args);

    }

}
