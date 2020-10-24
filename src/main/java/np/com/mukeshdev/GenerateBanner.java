package np.com.mukeshdev;

import com.github.lalyos.jfiglet.FigletFont;
import com.intellij.ide.IdeView;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import np.com.mukeshdev.constant.FontConstant;
import np.com.mukeshdev.service.BannerSettingService;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class GenerateBanner extends AnAction {

    private static final Logger log = Logger.getInstance(GenerateBanner.class);

    public static final String BANNER_TXT_FILE_NAME = "banner.txt";
    BannerSettingService bannerSettingService = new BannerSettingService();

    private static final NotificationGroup BALLOON_GROUP = new NotificationGroup(
            "generateBanner",
            NotificationDisplayType.STICKY_BALLOON, true
    );

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        try {

            Pair<String, Boolean> bannerTextAndIsDefaultPair = takeUserTextInput();

            if (bannerTextAndIsDefaultPair.first == null) {
                return;
            }

            String selectedBannerFonts = takeUserFontsInput();

            if (selectedBannerFonts == null) {
                return;
            }

            boolean fileCreated = createBannerFileWithSelectedFont(anActionEvent, bannerTextAndIsDefaultPair, selectedBannerFonts);
            if (!fileCreated)
                return;

            saveSettingAsDefaultIfRequired(bannerTextAndIsDefaultPair, selectedBannerFonts);

        } catch (Exception ex) {
            log.error(ex);
        }
    }

    private void saveSettingAsDefaultIfRequired(Pair<String, Boolean> bannerTextAndIsDefaultPair, String selectedBannerFonts) {
        if (bannerTextAndIsDefaultPair.second != null && bannerTextAndIsDefaultPair.second) {
            bannerSettingService.setDefaultBannerFonts(selectedBannerFonts);
            bannerSettingService.setDefaultBannerText(bannerTextAndIsDefaultPair.first);
        }
    }

    private String takeUserFontsInput() {
        String defaultBannerFonts = bannerSettingService.getDefaultBannerFonts();

        if (defaultBannerFonts == null || defaultBannerFonts.isEmpty()) {
            defaultBannerFonts = "Star Wars";
        }

        return Messages.showEditableChooseDialog(
                "Choose font", "Banner Font", null,
                FontConstant.fonts, defaultBannerFonts, null
        );
    }

    private Pair<String, Boolean> takeUserTextInput() {

        return Messages.showInputDialogWithCheckBox(
                "Banner text", "Banner Text",
                "Make this my default banner", false, true,
                null, bannerSettingService.getDefaultBannerText(), null
        );
    }


    private boolean createBannerFileWithSelectedFont(@NotNull AnActionEvent anActionEvent, Pair<String, Boolean> bannerTextAndIsDefaultPair,
                                                     String selectedBannerFonts) {

        try (
                InputStream fontsInputStream = GenerateBanner.class.getClassLoader().getResourceAsStream(
                        FontConstant.fontFolderLocation + "/" + selectedBannerFonts + FontConstant.fontExtension
                )
        ) {

            final IdeView selectedView = anActionEvent.getData(LangDataKeys.IDE_VIEW);
            if (selectedView == null) {
                return false;
            }

            final PsiDirectory selectedDir = selectedView.getOrChooseDirectory();

            if (selectedDir == null) {
                return false;
            }

            PsiFile bannerFile = WriteCommandAction.runWriteCommandAction(
                    anActionEvent.getProject(), createFile(selectedDir)
            );

            if (anActionEvent.getProject() == null) {
                return false;
            }

            Document bannerDocument = PsiDocumentManager.getInstance(anActionEvent.getProject()).getDocument(bannerFile);

            if (bannerDocument == null) {
                return false;
            }


            String bannerAsciiArt;

            if (fontsInputStream != null) {
                bannerAsciiArt = FigletFont.convertOneLine(fontsInputStream, bannerTextAndIsDefaultPair.first);
            } else {
                bannerAsciiArt = FigletFont.convertOneLine(bannerTextAndIsDefaultPair.first);
            }

            String finalBannerAsciiArt = bannerAsciiArt;
            Runnable runnable = () -> bannerDocument.setText(finalBannerAsciiArt);

            WriteCommandAction.runWriteCommandAction(
                    anActionEvent.getProject(), runnable
            );

            return true;
        } catch (Exception ex) {
            log.error(ex);
            Notification msg = new Notification(
                    BALLOON_GROUP.getDisplayId(), null,
                    "Generate Banner Error", "", ex.getMessage(),
                    NotificationType.INFORMATION, null
            );
            msg.notify(anActionEvent.getProject());
            return false;
        }
    }

    public Computable<PsiFile> createFile(PsiDirectory selectedDir) {
        return () -> selectedDir.createFile(BANNER_TXT_FILE_NAME);
    }
}
