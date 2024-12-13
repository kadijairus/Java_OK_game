package ee.taltech.okgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontManager {

    private static FontManager instance;
    private BitmapFont font10;
    private BitmapFont font12;
    private BitmapFont font14;
    private BitmapFont font16;
    private BitmapFont buttonFont24;
    private BitmapFont boldFont20;
    private BitmapFont boldFont28;

    private FontManager() {
        loadFonts();
    }

    // Method to get the single instance of the class
    public static synchronized FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    private void loadFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-Regular.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = 10;
        font10 = generator.generateFont(parameter);

        parameter.size = 12;
        font12 = generator.generateFont(parameter);

        parameter.size = 14;
        font14 = generator.generateFont(parameter);

        parameter.size = 16;
        font16 = generator.generateFont(parameter);

        generator.dispose(); // Dispose generator to avoid memory leaks

        FreeTypeFontGenerator buttonFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("LuckiestGuy-Regular.ttf"));
        FreeTypeFontParameter buttonFontParameter = new FreeTypeFontParameter();
        buttonFontParameter.size = 24;
        buttonFont24 = buttonFontGenerator.generateFont(buttonFontParameter);
        buttonFontGenerator.dispose();

        FreeTypeFontGenerator boldFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-Bold.ttf"));
        FreeTypeFontParameter boldFontParameter = new FreeTypeFontParameter();
        boldFontParameter.size = 20;
        boldFont20 = boldFontGenerator.generateFont(boldFontParameter);
        boldFontParameter.size = 28;
        boldFont28 = boldFontGenerator.generateFont(boldFontParameter);
        boldFontGenerator.dispose();

    }



    public BitmapFont getFontOpenSansRegular10() {
        return font10;
    }

    public BitmapFont getFontOpenSansRegular12() {
        return font12;
    }

    public BitmapFont getFontOpenSansRegular14() {
        return font14;
    }
    public BitmapFont getFontOpenSansRegular16() {
        return font16;
    }

    public BitmapFont getButtonFont24() {
        return buttonFont24;
    }

    public BitmapFont getBoldFont20() {
        return boldFont20;
    }
    public BitmapFont getBoldFont28() {
        return boldFont28;
    }



    public void dispose() {
        if (font10 != null) font10.dispose();
        if (font12 != null) font12.dispose();
        if (font14 != null) font14.dispose();
        if (font16 != null) font16.dispose();
        if (buttonFont24 != null) buttonFont24.dispose();
        if (boldFont20 != null) boldFont20.dispose();
        if (boldFont28 != null) boldFont28.dispose();
    }
}
