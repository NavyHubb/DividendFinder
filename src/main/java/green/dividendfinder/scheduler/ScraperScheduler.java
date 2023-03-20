package green.dividendfinder.scheduler;

import green.dividendfinder.model.Company;
import green.dividendfinder.model.ScrapedResult;
import green.dividendfinder.model.constants.CacheKey;
import green.dividendfinder.persist.entity.CompanyEntity;
import green.dividendfinder.persist.entity.DividendEntity;
import green.dividendfinder.persist.repository.CompanyRepository;
import green.dividendfinder.persist.repository.DividendRepository;
import green.dividendfinder.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;


    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)  // 캐시에서 finance로 시작되는 모든 데이터를 지운다
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("Scraping scheduler is started.");

        // 저장된 회사 목록 조회
        List<CompanyEntity> companies = companyRepository.findAll();

        // 회사별 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("Scraping scheduler is started. -> " + company.getName());
            ScrapedResult scrapedResult = yahooFinanceScraper.scrap(Company.builder()
                                                                            .name(company.getName())
                                                                            .ticker(company.getTicker())
                                                                            .build());

            // 스크래핑한 배당금 정보 중 DB에 없는 값이면 저장
            scrapedResult.getDividends().stream()
                    .map(e -> new DividendEntity(company.getId(), e))
                    .forEach(e -> {
                        boolean exists = dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 반복 사이에 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }


    //    @Scheduled(fixedDelay = 1000)
//    public void test1() throws InterruptedException {
//        Thread.sleep(3000);
//        System.out.println(Thread.currentThread().getName() + " -> 테스트1 : " + LocalDateTime.now());
//    }
//
//    @Scheduled(fixedDelay = 1000)
//    public void test2() throws InterruptedException {
//        System.out.println(Thread.currentThread().getName() + " -> 테스트2 : " + LocalDateTime.now());
//    }
}
