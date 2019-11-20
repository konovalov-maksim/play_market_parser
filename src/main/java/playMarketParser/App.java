package playMarketParser;

import java.util.Date;
import java.util.List;

public class App {

    public App(String appUrl) {
        setUrl(appUrl);
    }

    private String id;

    private String name;

    private String devId;

    private String devName;

    private String devWebSite;

    private String devEmail;

    private Integer installsCount;

    private Integer ratesCount;

    private Double avgRate;

    private Integer minAge;

    private String sizeMb;

    private String category;

    private String whatsNew;

    private Date lastUpdate;

    private String seller;

    private String version;

    private String minSdkVer;

    private List<String> similarApps;

    private Boolean containsAds;

    private Boolean offersPurchases;

    private String contentCost;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return "https://play.google.com/store/apps/details?id=" + id;
    }

    public void setUrl(String url) {
        try {
            id = url.split("=")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            id = url;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevWebSite() {
        return devWebSite;
    }

    public void setDevWebSite(String devWebSite) {
        this.devWebSite = devWebSite;
    }

    public String getDevEmail() {
        return devEmail;
    }

    public void setDevEmail(String devEmail) {
        this.devEmail = devEmail;
    }

    public Integer getInstallsCount() {
        return installsCount;
    }

    public void setInstallsCount(Integer installsCount) {
        this.installsCount = installsCount;
    }

    public Integer getRatesCount() {
        return ratesCount;
    }

    public void setRatesCount(Integer ratesCount) {
        this.ratesCount = ratesCount;
    }

    public Double getAvgRate() {
        return avgRate;
    }

    public void setAvgRate(Double avgRate) {
        this.avgRate = avgRate;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public String getSizeMb() {
        return sizeMb;
    }

    public void setSizeMb(String sizeMb) {
        this.sizeMb = sizeMb;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getWhatsNew() {
        return whatsNew;
    }

    public void setWhatsNew(String whatsNew) {
        this.whatsNew = whatsNew;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMinSdkVer() {
        return minSdkVer;
    }

    public void setMinSdkVer(String minSdkVer) {
        this.minSdkVer = minSdkVer;
    }

    public List<String> getSimilarApps() {
        return similarApps;
    }

    public void setSimilarApps(List<String> similarApps) {
        this.similarApps = similarApps;
    }

    public Boolean getContainsAds() {
        return containsAds;
    }

    public void setContainsAds(Boolean containsAds) {
        this.containsAds = containsAds;
    }

    public Boolean getOffersPurchases() {
        return offersPurchases;
    }

    public void setOffersPurchases(Boolean offersPurchases) {
        this.offersPurchases = offersPurchases;
    }

    public String getContentCost() {
        return contentCost;
    }

    public void setContentCost(String contentCost) {
        this.contentCost = contentCost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
