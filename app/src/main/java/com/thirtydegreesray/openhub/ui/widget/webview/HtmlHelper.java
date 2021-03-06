/*
 *    Copyright 2017 ThirtyDegreesRay
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.thirtydegreesray.openhub.ui.widget.webview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ThirtyDegreesRay on 2017/8/20 12:58:43
 */

class HtmlHelper {

    private final static List<String> SUPPORTED_CODE_FILE_EXTENSIONS = Arrays.asList(
            "bsh", "c", "cc", "cpp", "cs", "csh", "cyc", "cv", "htm", "html", "java",
            "js", "m", "mxml", "perl", "pl", "pm", "py", "rb", "sh", "xhtml", "xml",
            "xsl"
    );

    private static Pattern LINK_MATCHER = Pattern.compile("href=\"(.*?)\"");
    private static Pattern IMAGE_MATCHER = Pattern.compile("src=\"(.*?)\"");

    static String generateImageHtml(@NonNull String imageUrl, @NonNull String backgroundColor){
        return "<html>" +
                "<head>" +
                    "<style>" +
                        "img{height: auto; width: 100%;}" +
                        "body{background: " + backgroundColor + ";}" +
                    "</style>" +
                "</head>" +
                "<body><img src=\"" + imageUrl + "\"/></body>" +
                "</html>";
    }

    static String generateCodeHtml(@NonNull String codeSource, @Nullable String extension,
                                   boolean isDark, @NonNull String backgroundColor, boolean wrap){
        String skin = isDark ? "sons-of-obsidian" : "prettify";
        return generateCodeHtml(codeSource, extension, skin, backgroundColor, wrap);
    }

    private static String generateCodeHtml(@NonNull String codeSource, @Nullable String extension,
                                           @Nullable String skin, @NonNull String backgroundColor, boolean wrap ){
        return "<html>\n" +
                "<head>\n" +
                    "<meta charset=\"utf-8\" />\n" +
                    "<title>Code WebView</title>\n" +
                    "<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0;\"/>" +
                    "<script src=\"./core/run_prettify.js?autoload=true&amp;" +
                    "skin=" + skin + "&amp;" +
                    "lang=" + getExtension(extension) + "&amp;\" defer></script>\n" +
                    "<style>" +
                        "body {background: " + backgroundColor + ";}" +
                        ".prettyprint {background: " + backgroundColor + ";}" +
                        "pre.prettyprint { white-space: " + (wrap ? "pre-wrap" : "no-wrap") + "; }" +
                    "</style>" +
                "</head>\n" +
                "<body>\n" +
                    "<?prettify lang=" + getExtension(extension) + " linenums=false?>\n" +
                    "<pre class=\"prettyprint\">\n" +
                        formatCode(codeSource) +
                    "</pre>\n" +
                "</body>\n" +
                "</html>";
    }

    static String generateMdHtml(@NonNull String mdSource, @Nullable String baseUrl,
                                 boolean isDark, @NonNull String backgroundColor,
                                 @NonNull String accentColor){
        String skin = isDark ? "markdown_dark.css" : "markdown_white.css";
        //FIXME fix image url or link url which incompletely
        return generateMdHtml(mdSource, skin, backgroundColor, accentColor);
    }

    private static String generateMdHtml(@NonNull String mdSource, String skin,
                                         @NonNull String backgroundColor, @NonNull String accentColor){
        return "<html>\n" +
                "<head>\n" +
                    "<meta charset=\"utf-8\" />\n" +
                    "<title>Code WebView</title>\n" +
                    "<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\"/>" +
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"./" + skin + "\">\n" +
                    "<style>" +
                        "body{background: " + backgroundColor + ";}" +
                        "a {color:" + accentColor + " !important;}" +
                    "</style>" +
                "</head>\n" +
                "<body>\n" +
                    mdSource +
                "</body>\n" +
                "</html>";
    }

    private static String getExtension(@Nullable String extension){
        return SUPPORTED_CODE_FILE_EXTENSIONS.contains(extension) ? extension : "";
    }

    private static String formatCode(@NonNull String codeSource){
        if(StringUtils.isBlank(codeSource)) return  codeSource;
        return codeSource.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

}
