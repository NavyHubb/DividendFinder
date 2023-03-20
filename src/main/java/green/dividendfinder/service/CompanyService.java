package green.dividendfinder.service;

import green.dividendfinder.exception.impl.NoCompanyException;
import green.dividendfinder.model.Company;
import green.dividendfinder.model.Dividend;
import green.dividendfinder.model.ScrapedResult;
import green.dividendfinder.persist.entity.CompanyEntity;
import green.dividendfinder.persist.entity.DividendEntity;
import green.dividendfinder.persist.repository.CompanyRepository;
import green.dividendfinder.persist.repository.DividendRepository;
import green.dividendfinder.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = companyRepository.existsByTicker(ticker);

        if (exists) {
            throw new RuntimeException("Already exists ticker");
        } else {
            return storeCompanyAndDividend(ticker);
        }
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // ticker를 기준으로 회사를 스크래핑
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker" + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과를 DB에 저장
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);

        return company;
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);  // 자동완성 기능에 key만 사용될 뿐, value는 사용되지 않으므로 null로 처리
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);

        return companyEntities.stream()
                        .map(e -> e.getName())
                        .collect(Collectors.toList());
    }

    /**
     * keyword를 prefix로 갖는 단어들의 목록을 반환
     */
    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream().collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        CompanyEntity company = companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        // 찾은 회사에 해당하는 배당금 정보도 함께 삭제
        dividendRepository.deleteAllByCompanyId(company.getId());

        // 회사 정보 삭제
        companyRepository.delete(company);

        // 트라이에 있는 회사명 삭제
        deleteAutocompleteKeyword(company.getName());

        return company.getName();
    }
}
