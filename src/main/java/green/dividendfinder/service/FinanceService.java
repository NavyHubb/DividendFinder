package green.dividendfinder.service;

import green.dividendfinder.model.Company;
import green.dividendfinder.model.Dividend;
import green.dividendfinder.model.ScrapedResult;
import green.dividendfinder.persist.entity.CompanyEntity;
import green.dividendfinder.persist.entity.DividendEntity;
import green.dividendfinder.persist.repository.CompanyRepository;
import green.dividendfinder.persist.repository.DividendRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public ScrapedResult getDividendByCompanyName(String companyName) {
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
