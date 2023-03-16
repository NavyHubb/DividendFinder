package green.dividendfinder.web;

import green.dividendfinder.model.Company;
import green.dividendfinder.persist.entity.CompanyEntity;
import green.dividendfinder.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@PathVariable String keyword) {
//        var result = companyService.autocomplete(keyword);  // Trie를 이용한 구현
        var result = companyService.getCompanyNamesByKeyword(keyword);  // SQL의 LIKE를 이용한 구현

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> companies = companyService.getAllCompany(pageable);  // 다 가져올 필요가 없지? 페이저블 가보자

        return ResponseEntity.ok(companies);
    }

    /**
     * 회사 및 배당금 정보 추가
     */
    @PostMapping
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();

        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }

        Company company = companyService.save(ticker);
        companyService.addAutocompleteKeyword(company.getName());  // 자동완성에서 검색될 수 있도록 trie에도 함께 저장

        return ResponseEntity.ok(company);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCompany() {
        return null;
    }

}
