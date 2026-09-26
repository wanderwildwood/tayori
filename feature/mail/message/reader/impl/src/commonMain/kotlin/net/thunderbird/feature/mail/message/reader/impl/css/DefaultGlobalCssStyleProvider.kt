package net.thunderbird.feature.mail.message.reader.impl.css

import net.thunderbird.core.common.mail.html.HtmlSettings
import net.thunderbird.feature.mail.message.reader.api.css.CssClassNameProvider
import net.thunderbird.feature.mail.message.reader.api.css.CssStyleProvider
import net.thunderbird.feature.mail.message.reader.api.css.CssVariableNameProvider
import net.thunderbird.feature.mail.message.reader.api.css.GlobalCssStyleProvider
import org.intellij.lang.annotations.Language

internal class DefaultGlobalCssStyleProvider private constructor(
    cssClassNameProvider: CssClassNameProvider,
    cssVariableNameProvider: CssVariableNameProvider,
) : GlobalCssStyleProvider {
    // E Ink: Lato, the app's own face, served from its resources by K9WebViewClient; black text, black quote rules (upstream colours each quote depth, which sixteen greys
    // cannot tell apart), links black and underlined, and a size nearer MMD's body text.
    @Language("HTML")
    override val style: String = """
        |<style>
        |  @font-face { font-family: 'Lato'; font-weight: 400; font-style: normal;
        |    src: url('cid:tayori-font/lato_regular'); }
        |  @font-face { font-family: 'Lato'; font-weight: 700; font-style: normal;
        |    src: url('cid:tayori-font/lato_bold'); }
        |  @font-face { font-family: 'Lato'; font-weight: 400; font-style: italic;
        |    src: url('cid:tayori-font/lato_italic'); }
        |  @font-face { font-family: 'Lato'; font-weight: 700; font-style: italic;
        |    src: url('cid:tayori-font/lato_bold_italic'); }
        |  body { font-family: 'Lato', sans-serif; font-size: 1.05rem; color: #000; }
        |  a, a * { color: #000 !important; text-decoration: underline; }
        |  .clear:after {
        |    content: "";
        |    clear: both;
        |    display: block;
        |  }
        |  .${cssClassNameProvider.rootClassName} {
        |    display: block;
        |    user-select: auto;
        |    -webkit-user-select: auto;
        |  }
        |  .${cssClassNameProvider.rootClassName}.${cssClassNameProvider.mainContentClassName} {
        |    box-sizing: border-box;
        |    width: 100%;
        |    overflow-wrap: break-word;
        |    padding: 0 8px;
        |  }
        |  .${cssClassNameProvider.rootClassName}.${cssClassNameProvider.mainContentClassName} pre {
        |    white-space: pre-wrap;
        |  }
        |  .${cssClassNameProvider.rootClassName}.${cssClassNameProvider.mainContentClassName} blockquote {
        |    margin: auto 0 auto 0.8ex !important;
        |    padding-left: 1ex !important;
        |    border-left-width: 2px !important;
        |    border-left-style: solid !important;
        |    border-left-color: #000 !important;
        |  }
        |</style>
    """.trimMargin()

    internal class Factory(
        private val cssClassNameProvider: CssClassNameProvider,
        private val cssVariableNameProvider: CssVariableNameProvider,
    ) : GlobalCssStyleProvider.Factory {
        override fun create(htmlSettings: HtmlSettings): CssStyleProvider = DefaultGlobalCssStyleProvider(
            cssClassNameProvider = cssClassNameProvider,
            cssVariableNameProvider = cssVariableNameProvider,
        )
    }
}

// TODO(#10498): Remove when UseNewMessageReaderCssStyles is no longer required
internal class LegacyGlobalCssStyleProvider(useDarkMode: Boolean) : GlobalCssStyleProvider {
    @Language("HTML")
    override val style: String = if (useDarkMode) {
        """
        |<style type="text/css">
        |  * {
        |    background: #121212 !important;
        |    color: #F3F3F3 !important
        |  }
        |  :link, :link * { color: #CCFF33 !important }
        |  :visited, :visited * { color: #551A8B !important }
        |</style>
        """.trimMargin()
    } else {
        ""
    }

    internal class Factory : GlobalCssStyleProvider.Factory {
        override fun create(htmlSettings: HtmlSettings): CssStyleProvider = LegacyGlobalCssStyleProvider(
            useDarkMode = htmlSettings.useDarkMode,
        )
    }
}
