package green.dividendfinder.web;

import green.dividendfinder.model.ScrapedResult;
import green.dividendfinder.service.FinanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/finance")
public class FinanceController {

    private final FinanceService financeService;

    /**
     * 회사 정보 및 배당금 정보 조회
     */
    @GetMapping("/dividend/{companyName}")
    public ResponseEntity<?> searchFinance(@PathVariable String companyName) {
        var result = financeService.getDividendByCompanyName(companyName);

        return ResponseEntity.ok(result);
    }

}
