package green.dividendfinder.scraper;

import green.dividendfinder.model.Company;
import green.dividendfinder.model.Dividend;
import green.dividendfinder.model.ScrapedResult;
import green.dividendfinder.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {

    private static final String STATISTICAL_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400;  // 60 * 60 *24

    // 특정 회사의 배당금 정보 조회
    @Override
    public ScrapedResult scrap(Company company) {
        var scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;
            String url = String.format(STATISTICAL_URL, company.getTicker(), START_TIME, now);

            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsedDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsedDivs.get(0);  // 테이블(historical-prices) 전체

            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(Dividend.builder()
                                .date(LocalDateTime.of(year, month, day, 0, 0))
                                .dividend(dividend)
                                .build());
            }

            scrapedResult.setDividends(dividends);
        } catch (IOException e) {
            // TODO : 스크랩 실패 메세지

            e.printStackTrace();
        }

        return scrapedResult;
    }

    // 특정 회사 정보 조회
    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Element titleEle = document.getElementsByTag("h1").get(0);
            String title = titleEle.text().split(" - ")[1].trim();

            return Company.builder()
                        .name(title)
                        .ticker(ticker)
                        .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
