/*-------------------------------------------------------------------------------------------------------------------*\
|  Copyright (C) 2014 eBay Software Foundation                                                                        |
|                                                                                                                     |
|  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     |
|  with the License.                                                                                                  |
|                                                                                                                     |
|  You may obtain a copy of the License at                                                                            |
|                                                                                                                     |
|       http://www.apache.org/licenses/LICENSE-2.0                                                                    |
|                                                                                                                     |
|  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   |
|  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  |
|  the specific language governing permissions and limitations under the License.                                     |
\*-------------------------------------------------------------------------------------------------------------------*/

package com.paypal.selion.testcomponents;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.paypal.selion.annotations.WebTest;
import com.paypal.selion.platform.asserts.SeLionAsserts;
import com.paypal.selion.platform.grid.Grid;
import com.paypal.selion.platform.html.Button;
import com.paypal.selion.platform.html.Container;
import com.paypal.selion.platform.html.TextField;
import com.paypal.selion.testcomponents.BasicPageImpl;

public class BasicPageImplTest {

    private TestPage page;

    @BeforeClass(groups = { "functional", "unit" })
    public void beforeClass() {
        page = new TestPage();
    }

    @Test(groups = { "functional" })
    @WebTest
    public void testLoadObjectMapFromMap() throws InterruptedException, IOException {
        page = new TestPage();
        Grid.open("about:blank");
        RemoteWebDriver driver = (RemoteWebDriver) Grid.driver().getWrappedDriver();
        String script = getScript();
        driver.executeScript(script);
        Thread.sleep(4000);

        SeLionAsserts.assertEquals(page.getFieldXTextField().getValue(), "Congratulations, You have found fieldX",
                "YamlV1 TextField value retrieved successfully");
        SeLionAsserts.assertEquals(page.getContinueButton().getValue(), "Continue",
                "Button value retrieved successfully");

        SeLionAsserts.assertFalse(page.getHiddenButton().isVisible(), "Yaml Hidden button is actually hidden");
    }

    @Test(groups = { "functional" })
    @WebTest
    public void testContainer() throws InterruptedException, IOException {
        Grid.open("about:blank");
        RemoteWebDriver driver = (RemoteWebDriver) Grid.driver().getWrappedDriver();
        String script = getScript();
        driver.executeScript(script);
        Thread.sleep(4000);

        TestPage page = new TestPage("US");

        SeLionAsserts.assertEquals(page.getSelionContainer().getContainerButton().getValue(), "Button 1",
                "Yaml Button from Container at index 0 retrieved succesfully");
        SeLionAsserts.assertEquals(page.getSelionContainer(1).getContainerButton().getValue(), "Button 2",
                "Yaml Button from Container at index 1 retrieved succesfully");
    }

    @Test(groups = { "functional" })
    @WebTest
    public void testPageTitle() throws InterruptedException, IOException {
        Grid.open("about:blank");
        RemoteWebDriver driver = (RemoteWebDriver) Grid.driver().getWrappedDriver();
        String script = getScript();
        driver.executeScript(script);
        Thread.sleep(4000);

        TestPage page = new TestPage("US");

        SeLionAsserts.assertEquals(Grid.driver().getTitle(), page.getExpectedPageTitle(),
                "PageTitle Yaml value retrieved successfully");
    }

    @Test(groups = { "functional" })
    @WebTest
    public void testFallBackLocale() throws InterruptedException, IOException {

        TestPage page = new TestPage("FR");

        SeLionAsserts.assertEquals(page.getFieldXTextField().getLocator(), "//input[@id='fieldXId_FR']",
                "Yaml FR locator returned by SeLion");
        SeLionAsserts.assertEquals(page.getContinueButton().getLocator(), "//input[@id='submit.x']",
                "Yaml US locator returned by SeLion because FR isn't set");
    }

    @Test(groups = { "functional" })
    @WebTest
    public void testPageValidator() throws InterruptedException, IOException {
        Grid.open("about:blank");
        RemoteWebDriver driver = (RemoteWebDriver) Grid.driver().getWrappedDriver();
        String script = getScript();
        driver.executeScript(script);
        Thread.sleep(4000);

        TestPage page = new TestPage("US");
        TestPage pageNotOpened = new TestPage("US", "TestWrongValidatorPage");
        TestPage pageTitleValidation = new TestPage("US", "PageTitleValidationPage");

        SeLionAsserts.assertEquals(page.isCurrentPageInBrowser(), true, "Page is opened in the browser");
        SeLionAsserts.assertEquals(pageNotOpened.isCurrentPageInBrowser(), false, "Page is not opened in the browser");
        // Validate the page by pageTitle, which is the fallback if there are no pageValidators provided.
        SeLionAsserts
                .assertEquals(pageTitleValidation.isCurrentPageInBrowser(), true, "Page is opened in the browser");

        pageTitleValidation.setPageTitle("Incorrect page title");
        SeLionAsserts.assertEquals(pageTitleValidation.isCurrentPageInBrowser(), false,
                "Page is not opened in the browser");

        pageTitleValidation.setPageTitle("* JavaScript");
        SeLionAsserts
                .assertEquals(pageTitleValidation.isCurrentPageInBrowser(), true, "Page is opened in the browser");

        pageTitleValidation.setPageTitle("* title");
        SeLionAsserts.assertEquals(pageTitleValidation.isCurrentPageInBrowser(), false,
                "Page is not opened in the browser");
    }

    @Test(groups = { "functional" })
    @WebTest
    public void testLoadHtmlObjectsWithContainer() {
        TestInitializeElementsPage testInitPage = new TestInitializeElementsPage();

        // Validations to verify valid parent types and elements are resolved as a result of initialization
        SeLionAsserts.assertTrue(testInitPage.getHeaderContainer() != null, "Verify Container is loaded properly");
        SeLionAsserts.assertTrue(
                testInitPage.getPreLoginButton().getParent().getClass().getSuperclass().equals(BasicPageImpl.class),
                "Verify if a page is assigned for element outside container");
        SeLionAsserts.assertTrue(testInitPage.getHeaderContainer().getSomeLink().getParent().getClass()
                .getSuperclass().equals(Container.class),
                "Verify if a Container is assigned for element inside container");
    }

    public class TestPage extends BasicPageImpl {

        private TextField fieldXTextField;
        private Button continueButton;
        private Button hiddenButton;
        private SeLionContainer selionContainer;

        private String CLASS_NAME = "TestPage";

        private String PAGE_DOMAIN = "paypal";

        public TestPage() {
            super.initPage(PAGE_DOMAIN, CLASS_NAME);
        }

        public TestPage(String siteLocale) {
            super.initPage(PAGE_DOMAIN, CLASS_NAME, siteLocale);
        }

        public TestPage(String siteLocale, String className) {
            super.initPage(PAGE_DOMAIN, className, siteLocale);
        }

        public TestPage getPage() {
            if (!isInitialized()) {
                loadObjectMap();
                initializeHtmlObjects(this, this.objectMap);
            }
            return this;
        }

        public Button getContinueButton() {
            return getPage().continueButton;
        }

        public void clickContinueButton(Object... expected) {
            getPage().continueButton.click(expected);
        }

        public void clickContinueButton() {
            getPage().continueButton.click();
        }

        public String getContinueButtonValue() {
            return getPage().continueButton.getText();
        }

        public Button getHiddenButton() {
            return getPage().hiddenButton;
        }

        public void clickHiddenButton(Object... expected) {
            getPage().hiddenButton.click(expected);
        }

        public void clickHiddenButton() {
            getPage().hiddenButton.click();
        }

        public String getHiddenButtonValue() {
            return getPage().hiddenButton.getText();
        }

        public TextField getFieldXTextField() {
            return getPage().fieldXTextField;
        }

        public void setFieldXTextFieldValue(String value) {
            getPage().fieldXTextField.type(value);
        }

        public String getFieldXTextFieldValue() {
            return getPage().fieldXTextField.getText();
        }

        public SeLionContainer getSelionContainer() {
            return getPage().selionContainer;
        }

        public SeLionContainer getSelionContainer(int index) {
            getPage().selionContainer.setIndex(index);
            return selionContainer;
        }

        public class SeLionContainer extends Container {

            private Button containerButton;

            public SeLionContainer(String locator) {
                super(locator);
            }

            public SeLionContainer(String locator, String controlName) {
                super(locator, controlName);
            }

            private SeLionContainer getContainer() {
                if (!isInitialized()) {
                    loadObjectMap();
                    initializeHtmlObjects(this, TestPage.this.objectMap);
                }
                return this;
            }

            public Button getContainerButton() {
                return getContainer().containerButton;
            }
        }

        public void setPageTitle(String pageTitle) {
            getPage().pageTitle = pageTitle;
        }
    }

    private String getScript() throws IOException {
        File scriptFile = new File("src/test/resources/testdata/InsertHtmlElements.js");
        return FileUtils.readFileToString(scriptFile);
    }
}
