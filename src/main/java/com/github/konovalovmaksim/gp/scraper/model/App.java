package com.github.konovalovmaksim.gp.scraper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class App {

    public App(){
    }

    public App(String appUrl) {
        setUrl(appUrl);
    }

    private String id;

    private String name;

    private String devUrl;

    private String devName;

    private String devWebSite;

    private String devEmail;

    private String devAddress;

    private Long installsCount;

    private Integer ratesCount;

    private Double avgRate;

    private String iconUrl;

    private String minAge;

    private String sizeMb;

    private String category;

    private String whatsNew;

    private String lastUpdate;

    private String releaseDate;

    private String version;

    private String minSdkVer;

    private List<String> similarApps = new ArrayList<>();

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

    public void reset() {
        this.name = null;
        this.devUrl = null;
        this.devName = null;
        this.devWebSite = null;
        this.devEmail = null;
        this.installsCount = null;
        this.ratesCount = null;
        this.avgRate = null;
        this.minAge = null;
        this.sizeMb = null;
        this.category = null;
        this.whatsNew = null;
        this.lastUpdate = null;
        this.version = null;
        this.minSdkVer = null;
        this.containsAds = null;
        this.offersPurchases = null;
        this.contentCost = null;
        this.description = null;
        this.releaseDate = null;
        this.similarApps.clear();
    }

    public String getUrl() {
        return id != null ? "https://play.google.com/store/apps/details?id=" + id : null;
    }

    public void setUrl(String url) {
        if (url.split("=").length == 2) id = url.split("=")[1];
        else id = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevUrl() {
        return devUrl;
    }

    public void setDevUrl(String devUrl) {
        this.devUrl = devUrl;
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

    public String getDevAddress() {
        return devAddress;
    }

    public void setDevAddress(String devAddress) {
        this.devAddress = devAddress;
    }

    public Long getInstallsCount() {
        return installsCount;
    }

    public void setInstallsCount(Long installsCount) {
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

    public String getMinAge() {
        return minAge;
    }

    public void setMinAge(String minAge) {
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

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
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

    public Integer getContainsAds() {
        if (containsAds == null) return null;
        return containsAds ? 1 : 0;
    }

    public void setContainsAds(Boolean containsAds) {
        this.containsAds = containsAds;
    }

    public Integer getOffersPurchases() {
        if (offersPurchases == null) return null;
        return offersPurchases ? 1 : 0;
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

    public String getSimApp1() {
        return similarApps != null && similarApps.size() > 0 ? similarApps.get(0) : null;
    }

    public String getSimApp2() {
        return similarApps != null && similarApps.size() > 1 ? similarApps.get(1) : null;
    }

    public String getSimApp3() {
        return similarApps != null && similarApps.size() > 2 ? similarApps.get(2) : null;
    }

    public String getSimApp4() {
        return similarApps != null && similarApps.size() > 3 ? similarApps.get(3) : null;
    }

    public void addSimilarApp(String simApp) {
        similarApps.add(simApp);
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return Objects.equals(id, app.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
