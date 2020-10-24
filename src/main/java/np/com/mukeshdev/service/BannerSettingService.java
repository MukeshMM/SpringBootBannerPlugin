package np.com.mukeshdev.service;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(
        name = "BannerSettingServiceComponent",
        storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)},
        defaultStateAsResource = true
)
public class BannerSettingService implements PersistentStateComponent<BannerSettingService> {

    public String defaultBannerText;
    public String defaultBannerFonts;

    @Override
    public BannerSettingService getState() {
        return this;
    }

    @Override
    public void loadState(BannerSettingService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getDefaultBannerText() {
        return defaultBannerText;
    }

    public void setDefaultBannerText(String defaultBannerText) {
        this.defaultBannerText = defaultBannerText;
    }

    public String getDefaultBannerFonts() {
        return defaultBannerFonts;
    }

    public void setDefaultBannerFonts(String defaultBannerFonts) {
        this.defaultBannerFonts = defaultBannerFonts;
    }
}
