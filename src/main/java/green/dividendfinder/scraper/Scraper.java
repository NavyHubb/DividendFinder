package green.dividendfinder.scraper;

import green.dividendfinder.model.Company;
import green.dividendfinder.model.ScrapedResult;

public interface Scraper {

    ScrapedResult scrap(Company company);
    Company scrapCompanyByTicker(String ticker);

}
