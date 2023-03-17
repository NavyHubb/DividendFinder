package green.dividendfinder.service;

import green.dividendfinder.model.Company;
import green.dividendfinder.model.Dividend;
import green.dividendfinder.model.ScrapedResult;
import green.dividendfinder.model.constants.CacheKey;
import green.dividendfinder.persist.entity.CompanyEntity;
import green.dividendfinder.persist.entity.DividendEntity;
import green.dividendfinder.persist.repository.CompanyRepository;
import green.dividendfinder.persist.repository.DividendRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // 요청이 자주 들어오는가? Y
    // 자주 변경되지 않는 데이터인가? Y
    // -> 캐싱 진행시켜
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)  // value값은 레디스 서버의 key의 prefix로 사용돼
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        // 1. 회사명을 기준으로 회사 정보 조회
        CompanyEntity companyEntity = companyRepository.findByName(companyName)
                                            .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = dividendRepository.findAllByCompanyId(companyEntity.getId());

        // 3. 결과 조합 후 반환
        Company company = Company.builder()
                                .name(companyEntity.getName())
                                .ticker(companyEntity.getTicker())
                                .build();

        List<Dividend> dividends = dividendEntities.stream()
                                                .map(e -> Dividend.builder()
                                                        .date(e.getDate())
                                                        .dividend(e.getDividend())
                                                        .build())
                                                .collect(Collectors.toList());

        return new ScrapedResult(company, dividends);
    }
}
