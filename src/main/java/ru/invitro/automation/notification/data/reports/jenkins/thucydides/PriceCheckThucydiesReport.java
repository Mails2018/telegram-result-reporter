package ru.invitro.automation.notification.data.reports.jenkins.thucydides;

import ru.invitro.automation.notification.data.prices.PriceCheckXml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PriceCheckThucydiesReport extends AttachThucydiesReport {

    private String noPriceIn1CPath = "no-price-in-1C %date.xml";

    private String wrongPricesPath = "wrong-prices %date.xml";

    private String unavailableCitiesPath = "unavailable-cities %date.xml";

    public PriceCheckThucydiesReport(String url, boolean isSmoke, boolean jobBasedReport, String id) {
        super(url, isSmoke, jobBasedReport, id);
    }

    @Override
    protected List<File> getFiles() {
        List<File> files = new ArrayList<>();
        if (reportAvailable) {
            if (getXmlReport().size() > 0) {
                PriceCheckXml xml = new PriceCheckXml(getXmlReport(), id);
                xml.parse();
                if (!xml.getOldProduct().isEmpty()) {
                    File oldProduct = createFile(fileNameWithDate(noPriceIn1CPath), xml.getOldProduct());
                    files.add(oldProduct);
                }
                if (!xml.getWrongPrice().isEmpty()) {
                    File wrongPrice = createFile(fileNameWithDate(wrongPricesPath), xml.getWrongPrice());
                    files.add(wrongPrice);
                }
                if (!xml.getUnavailableCity().isEmpty()) {
                    File unavailableCity = createFile(fileNameWithDate(unavailableCitiesPath), xml.getUnavailableCity());
                    files.add(unavailableCity);
                }
            }
        }
        return files;
    }

    private List<String> getXmlReport() {
        return xmlReport;
    }
}
