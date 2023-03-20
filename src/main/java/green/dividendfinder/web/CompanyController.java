package green.dividendfinder.web;

import green.dividendfinder.model.Company;
import green.dividendfinder.model.constants.CacheKey;
import green.dividendfinder.persist.entity.CompanyEntity;
import green.dividendfinder.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
//        var result = companyService.autocomplete(keyword);  // Trie를 이용한 구현
        var result = companyService.getCompanyNamesByKeyword(keyword);  // SQL의 LIKE를 이용한 구현

        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('READ', 'WRITE')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> companies = companyService.getAllCompany(pageable);  // 다 가져올 필요가 없지? 페이저블 가보자

        return ResponseEntity.ok(companies);
    }

    /**
     * 회사 및 배당금 정보 추가
     */
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")  // 관리 권한을 가진 사용자만 이 API를 호출할 수 있도록
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();

        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }

        Company company = companyService.save(ticker);
        companyService.addAutocompleteKeyword(company.getName());  // 자동완성에서 검색될 수 있도록 trie에도 함께 저장

        log.info("company inserted -> " + company.getName());

        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")  // 관리 권한을 가진 사용자만 이 API를 호출할 수 있도록
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = companyService.deleteCompany(ticker);

        // 캐시에서도 지워줘야지
        clearFinanceCache(companyName);

        log.info("company delted -> " + companyName);

        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName) {
        redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}