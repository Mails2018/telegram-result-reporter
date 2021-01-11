package ru.invitro.automation.notification.data.prices;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PriceCheckXml {

    private List<String> reports;

    private Map<City, List<Product>> oldProduct = new TreeMap<>();

    private Map<City, List<Product>> siteUnavailable = new TreeMap<>();

    private Map<City, List<Product>> wrongPrice = new TreeMap<>();

    private String id;

    public PriceCheckXml(List<String> reports, String operationID) {
        this.reports = reports;
        this.id = operationID;
    }

    public void parse() {
        Logger.writeLog("XML from report: \n" + reports, id);
        for (String report : reports) {
            DocumentBuilder ndb;
            NodeList citiesNodes = null;
            NodeList productNodes = null;
            try {
                ndb = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                citiesNodes = ndb.parse(new InputSource(new StringReader(report))).getDocumentElement().getElementsByTagName("City");
                productNodes = ndb.parse(new InputSource(new StringReader(report))).getDocumentElement().getElementsByTagName("list");
                Logger.writeLog("XML parse success", id);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                Logger.writeLog("Error in XML parse\n" + e.getMessage(), id);
                e.printStackTrace();
            }
            for (int i = 0; i < citiesNodes.getLength(); i++) {
                Document cityDocument = nodeToDocument(citiesNodes.item(i));
                Document productListDocument = nodeToDocument(productNodes.item(i));
                NodeList products = productListDocument.getDocumentElement().getElementsByTagName("Product");
                NodeList cityNameNode = cityDocument.getDocumentElement().getElementsByTagName("name");
                NodeList guidNode = cityDocument.getDocumentElement().getElementsByTagName("GUID");
                String cityName = cityNameNode.item(0).getTextContent();
                String guid = guidNode.item(0).getTextContent();
                City city = new City(cityName, guid);
                List<Product> cityOldProducts = new ArrayList<>();
                List<Product> cityUnavailable = new ArrayList<>();
                List<Product> cityWrongPrices = new ArrayList<>();
                for (int j = 0; j < products.getLength(); j++) {
                    Document productDocument = nodeToDocument(products.item(j));
                    NodeList id = productDocument.getDocumentElement().getElementsByTagName("id");
                    NodeList priceWeb = productDocument.getDocumentElement().getElementsByTagName("priceWeb");
                    NodeList price1C = productDocument.getDocumentElement().getElementsByTagName("price1C");
                    Product product = new Product(id.item(0).getTextContent(), priceWeb.item(0).getTextContent(), price1C.item(0).getTextContent());
                    if (product.getPrice1C().trim().equals("none")) {
                        cityOldProducts.add(product);
                        continue;
                    }
                    if (product.getPrice1C().trim().equals("unavailable") || product.getPrice1C().equals("not synchronized")) {
                        cityUnavailable.add(product);
                        continue;
                    }
                    cityWrongPrices.add(product);
                }
                if (cityOldProducts.size() > 0) {
                    oldProduct.put(city, cityOldProducts);
                }
                if (cityUnavailable.size() > 0) {
                    siteUnavailable.put(city, cityUnavailable);
                }
                if (cityWrongPrices.size() > 0) {
                    wrongPrice.put(city, cityWrongPrices);
                }
            }
        }
    }

    public String getOldProduct() {
        if (oldProduct.isEmpty()) {
            return "";
        }
        return mapToXlm(oldProduct);
    }

    public String getUnavailableCity() {
        if (siteUnavailable.isEmpty()) {
            return "";
        }
        return mapToXlm(siteUnavailable);
    }

    public String getWrongPrice() {
        if (wrongPrice.isEmpty()) {
            return "";
        }
        return mapToXlm(wrongPrice);
    }

    private Document nodeToDocument(Node node) {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            Logger.writeLog(e.getMessage(), id);
            e.printStackTrace();
        }
        Node productNode = document.importNode(node, true);
        document.appendChild(productNode);
        return document;
    }

    private String mapToXlm(Map<City, List<Product>> map) {
        XStream xStream = new XStream(new DomDriver("UTF-8"));
        xStream.alias("errorInPrices", java.util.Map.class);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + xStream.toXML(map).
            replaceAll("ru\\.invitro\\.automation\\.notification\\.data\\.", "").
            replaceAll("Data\\.", "").
            replaceAll("list", "ProductList");
    }
}
