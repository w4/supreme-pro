package uk.jordandoyle.supreme

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import java.net.URL
import java.util.concurrent.ThreadLocalRandom
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.interactions.Actions
import java.util.*
import kotlin.system.exitProcess

/**
 * This xpath returns the "shop" link on the homepage
 */
const val SHOP_LINK_XPATH = "//a[span[contains(., 'shop')]]"

/**
 * This xpath will return every product on the products page
 */
const val PRODUCT_XPATH = "//div[@id='container']//ul/li/a[span[contains(., 'new')]]"

/**
 * This xpath will return the "add to basket" form, this form doesn't
 * exist if the product has sold out
 */
const val ADD_TO_BASKET_XPATH = "//form[contains(@action, '/add')]//input[contains(@value, 'add to basket')]"

fun main(args: Array<String>) {
    // use a remote gecko session so Selenium's built in server doesn't add detectable cache global variables
    val driver = RemoteWebDriver(URL("http://127.0.0.1:4444"), DesiredCapabilities.firefox())

    val builder = Actions(driver)
    val wait = WebDriverWait(driver, 10)

    // browse from the homepage
    driver.get("http://google.com/")
    driver.findElementByXPath("//input[@type='text']").sendKeys("supreme")
    driver.findElementByXPath("//input[@value='Google Search']").click()
    driver.findElementByLinkText("Supreme").click()

    // sleep for a little bit then go to their shop
    randomSleep()
    builder.moveToElement(driver.findElementByXPath(SHOP_LINK_XPATH)).click().build().perform()

    println("Sleeping until Browse page loads")
    wait.until(ExpectedConditions.elementToBeClickable(By.xpath(PRODUCT_XPATH)))

    // loop over each product
    for (i in driver.findElementsByXPath(PRODUCT_XPATH).indices) {
        // click the next product in the list
        builder.moveToElement(driver.findElementsByXPath(PRODUCT_XPATH)[i]).click().build().perform()

        println("New product: " + driver.currentUrl)

        println("   Sleeping until Product page loads")
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("details")))

        try {
            randomSleep()
            builder.moveToElement(driver.findElementByXPath(ADD_TO_BASKET_XPATH)).click().build().perform()
            break
        } catch (e: NoSuchElementException) {
            println("   This item isn't available.")

            driver.navigate().back()
            println("   Sleeping until Browse page loads")
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(PRODUCT_XPATH)))
            continue
        }
    }

    try {
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElementByLinkText("checkout now")))
    } catch (e: Exception) {
        println("Didn't find any available products.")
        exitProcess(1)
    }

    builder.moveToElement(driver.findElementByLinkText("checkout now")).click().build().perform()

    println("Sleeping until Checkout page loads")
    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[label[contains(., 'full name')]]/input")))

    for (field in Arrays.asList("full name", "email", "tel", "address", "city", "postcode", "number", "CVV")) {
        println("Filling $field")

        builder.moveToElement(driver.findElementByXPath("//div[*[contains(., '$field')]]/input"))
                .click()
                .sendKeys("this is my full name")
                .build()
                .perform()
    }
}

/**
 * Sleep for a little while before performing an action.
 */
fun randomSleep() {
    val len = ThreadLocalRandom.current().nextInt(800, 1200).toLong()
    println("   Sleeping for ${len}ms.")
    Thread.sleep(len)
}