package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.Duration;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        System.setProperty("webdriver.chrome.driver", "C:\\drivers\\chromedriver-win64\\chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        String timestamp = String.valueOf(System.currentTimeMillis());

        try {

            driver.get("https://maps.google.com");
            driver.manage().window().maximize();


            try {
                WebElement accept = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Accept')]")));
                accept.click();
            } catch (Exception ignored) {}


            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-value='Directions']"))).click();


            WebElement startInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@aria-label='Choose starting point, or click on the map...']")));
            startInput.sendKeys("Andheri West, Mumbai");
            startInput.sendKeys(Keys.ENTER);


            WebElement destInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@aria-label='Choose destination, or click on the map...']")));
            destInput.sendKeys("91 Springboard, Vikhroli");
            destInput.sendKeys(Keys.ENTER);


            WebElement firstRoute = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'section-directions-trip')]")));
            firstRoute.click();


            Thread.sleep(90000);
            List<WebElement> steps = driver.findElements(
                    By.xpath("//div[contains(@class,'directions-mode-step')]/div[@class='directions-step-description']"));

            if (steps.isEmpty()) {
                System.out.println("No driving instructions found.");
                return;
            }


            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Driving Instructions");

            int rowNum = 0;
            for (WebElement step : steps) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(step.getText());
            }
            sheet.autoSizeColumn(0);

            String excelPath = "driving_instructions_" + timestamp + ".xlsx";
            try (FileOutputStream fileOut = new FileOutputStream(excelPath)) {
                workbook.write(fileOut);
            }
            workbook.close();

            System.out.println(" Instructions saved to " + excelPath);


            File fullShot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileHandler.copy(fullShot, new File("screenshot_" + timestamp + ".png"));
            System.out.println(" Full screenshot saved.");


            try {
                WebElement directionsPanel = driver.findElement(By.xpath("//div[contains(@class, 'section-directions-trip-details')]"));
                File panelShot = directionsPanel.getScreenshotAs(OutputType.FILE);
                FileHandler.copy(panelShot, new File("directions_only_" + timestamp + ".png"));
                System.out.println(" Directions panel screenshot saved.");
            } catch (Exception e) {
                System.out.println(" Could not capture directions panel: " + e.getMessage());
            }

        } finally {
            Thread.sleep(3000);
            driver.quit();
        }
    }
}
