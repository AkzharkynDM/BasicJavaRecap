import static org.testng.Assert.assertEquals;
import java.security.SecureRandom;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
	
public class LoginTests {
	// Object of Connection from the Database
	private Connection conn = null;
 
	// Object of Statement. It is used to create a Statement to execute the query
	private Statement stmt = null;
 
	//Object of ResultSet => 'It maintains a cursor that points to the current row in the result set'
	private ResultSet resultSet = null;
	private WebDriver webdriver;
	private AppiumDriver<MobileElement> androiddriver=null;
	private AppiumDriver<MobileElement> driver=null;
	private AppiumDriverLocalService service=null;
	private AppiumServiceBuilder builder=null;
	
	@BeforeTest
	public void SetUpConnection() throws SQLException, ClassNotFoundException {
	
		// Register JDBC driver (JDBC driver name and Database URL)
		Class.forName("org.sqlite.JDBC");
 
		//create a jdbc connection to book.db located in below file path
		String path="jdbc:sqlite:E:\\test\\usersManager.db";
		conn = DriverManager.getConnection(path);
		conn.setAutoCommit(false);
		System.setProperty("webdriver.chrome.driver", "<Path of Driver>\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
 
		// Code to disable the popup of saved password
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("credentials_enable_service", false);
		prefs.put("password_manager_enabled", false);
		options.setExperimentalOption("prefs", prefs);
		webdriver = new ChromeDriver(options);
		webdriver.get("<URL>");
		
		
			//Set the Desired Capabilities
			DesiredCapabilities caps = new DesiredCapabilities();
			/*caps.setCapability("deviceName", "My Phone");
			caps.setCapability("udid", "ENUL6303030010"); //Give Device ID of your mobile phone
			caps.setCapability("platformName", "Android");
			caps.setCapability("platformVersion", "6.0");*/
			caps.setCapability("BROWSER_NAME", "Android"); 
			caps.setCapability("VERSION", "7.1.1");  
			caps.setCapability("deviceName","Android Emulator"); 
			caps.setCapability("platformName","Android");
			caps.setCapability("avd","Nexus_5X_API_25");     
			caps.setCapability("appPackage", "com.android.smartreminder");
			caps.setCapability("appActivity", "com.google.android.smartreminder.MainActivity");
			caps.setCapability("noReset", "true");
			
			//Build the Appium service
			builder = new AppiumServiceBuilder();
			builder.withIPAddress("127.0.0.1");
			builder.usingPort(4723);
			builder.usingDriverExecutable(new File("C:\\Program Files\\nodejs\\node.exe"));
			builder.withAppiumJS(new File("C:\\Users\\User\\AppData\\Roaming\\npm\\node_modules\\appium\\build\\lib\\main.js"));
			//builder.withLogFile(new File("C:\\Users\\User\\workspace\\AppiumTest\\ConsoleLogs\\AppiumConsoleLogs.txt")));
			
			builder.withCapabilities(caps);
			builder.withArgument(GeneralServerFlag.SESSION_OVERRIDE);
			builder.withArgument(GeneralServerFlag.LOG_LEVEL,"error");
					
			service = AppiumDriverLocalService.buildService(builder);
				
			service.start();		
			
			//Instantiate Appium Driver			
			try {
					driver = new AndroidDriver<MobileElement>(new URL("http://0.0.0.0:4723/wd/hub"), caps);
				
			} catch (MalformedURLException e) {
				System.out.println(e.getMessage());
			}
		
		//Instantiate Appium Driver			
		try {
			androiddriver = new AndroidDriver<MobileElement>(new URL("http://0.0.0.0:4723/wd/hub"), caps);
					
		} catch (MalformedURLException e) {
				System.out.println(e.getMessage());
		}
	}
 
	@Test
	public void OperationalMethod() {
		
		MobileElement login=androiddriver.findElement(By.name("Login"));
		login.click();  
		
		try {
			// Execute a query
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery("select * from contacts");
 
			// Get the all row of UI Table
			List<WebElement> lstTr = webdriver.findElement(By.id("grdData")).findElements(By.tagName("tr"));
 
			// Index for Row
			int rowCount = 0;
 
			// Count of Matched Column
			int matchColumnCount = 0;
 
			// Count of Matched Row
			int matchRowCount = 0;
 
			System.out.println("Row Count => " + lstTr.size());
 
			// Extract the data from Table
			while (resultSet.next()) {
 
			
			//(rowCount + 1) because first row is a header row , Get all the columns from a particular row
			List<WebElement> lstTd = lstTr.get(rowCount + 1).findElements(By.tagName("td"));
			System.out.println("Cloumn Count => " + lstTd.size());
 
			for (int j = 0; j < lstTd.size(); j++) {
				String uiCell = lstTd.get(j).getText();
				System.out.println("UI Cell Data => " + uiCell);
 
				/*
				* (j + 1) in the resultSet => because index is start from 1
				* and here loop is starting from 0
				*/
				String dbCell = resultSet.getString(j + 1);
				System.out.println("DB Cell Data => " + dbCell);
 
				// Comparison between both string
				if (uiCell.trim().equalsIgnoreCase(dbCell.trim())) {
					matchColumnCount++;
				}
			}
				
			if (matchColumnCount == lstTd.size()) {
				matchRowCount++;
				System.out.println("========ROW MATCHED==========");
			}
 
			// Set 'matchColumnCount' to 0 for next row	matchColumnCount = 0;
				rowCount++;
			}
				assertEquals(matchRowCount, rowCount, "UI Table is the exact copy of Database Table");
		} catch (Exception e) {
				System.out.println(e);
		}
	}
 
	@AfterTest
	public void CloseTheConnection() throws SQLException {
	
	// Code to close each and all Object related to Database connection
	if (resultSet != null) {
		try {
			resultSet.close();
		} catch (Exception e) {
		}
	}
	
	if (stmt != null) {
		try {
			stmt.close();
		} catch (Exception e) {
		}
	}
	
	if (conn != null) {
		try {
			conn.close();
		} catch (Exception e) {
		}
	}
	
	webdriver.quit();
	androiddriver.quit();
	}
 
}