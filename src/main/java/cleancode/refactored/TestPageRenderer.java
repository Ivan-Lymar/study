package cleancode.refactored;

import cleancode.PageCrawlerImpl;
import cleancode.PageData;
import cleancode.PathParser;
import cleancode.SuiteResponder;
import cleancode.WikiPage;
import cleancode.WikiPagePath;

//inspired by https://qntm.org/clean
class TestSuiteRendererFactory {
    public static TestPageRenderer getRenderer(PageData pageData) {
        return getRenderer(pageData, false);
    }

    public static TestPageRenderer getRenderer(PageData pageData, boolean isSuite) {
        if (isSuite) {
            return new TestSuiteRenderer(pageData);
        }
        return new TestPageRenderer(pageData);
    }
}

final class TestSuiteRenderer extends TestPageRenderer {
    TestSuiteRenderer(PageData pageData) {
        super(pageData);
    }

    @Override
    String includeSetupPages() {
        return generate(SuiteResponder.SUITE_SETUP_NAME, "-setup") + super.includeSetupPages();
    }

    @Override
    String includeTeardownPages() {
        return super.includeTeardownPages() + generate(SuiteResponder.SUITE_TEARDOWN_NAME, "-teardown");
    }
}

public class TestPageRenderer {
    private final PageData pageData;

    TestPageRenderer(PageData pageData) {
        this.pageData = pageData;
    }

    String render() {
        if (isTestPage()) {
            return regenerateWithSetupAndTeardownPages();
        }

        return pageData.getHtml();
    }

    private boolean isTestPage() {
        return pageData.hasAttribute("Test");
    }

    private String regenerateWithSetupAndTeardownPages() {
        return includeSetupPages() +
                String.valueOf(pageData.getContent()) +
                includeTeardownPages();
    }

    String includeSetupPages() {
        return generate("SetUp", "-setup");
    }

    String includeTeardownPages() {
        return generate("TearDown", "-teardown");
    }

    String generate(String pageName, String arg) {
        WikiPage inheritedPage = PageCrawlerImpl.getInheritedPage(pageName, pageData.getWikiPage());
        if (inheritedPage != null) {
            String pagePathName = getPathNameForPage(inheritedPage);
            return String.format("\n!include %s .%s \n", arg, pagePathName);
        }

        return "";
    }

    private String getPathNameForPage(WikiPage page) {
        WikiPagePath pagePath = page.getPageCrawler().getFullPath(page);
        return PathParser.render(pagePath);
    }

}
