package pro.scraping.captcha.solve;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.NoSuchElementException;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Exception;
import com.DeathByCaptcha.SocketClient;


public class CaptchaSolver {

 static {
  logger = Logger.getLogger(CaptchaSolver.class.getName());
 }

 public static void main(String[] args) {
  double tDelta;
   
  long t1 = System.currentTimeMillis();

  Integer tentativas = 0;
  Integer erro_captcha = 0;
  try {

   //			logger.info("Initializing the Firefox webdriver...");
   System.out.println("Inicio");
   FirefoxDriver driver = new FirefoxDriver();

   long t2 = System.currentTimeMillis();
   tDelta = (t2 - t1) / 1000.0;
   System.out.println("abrir firefox driver: " + String.valueOf(tDelta));

   //driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);			

   DriverManager.registerDriver(new org.postgresql.Driver());

   Connection conn = null;
   String dbURL = "jdbc:postgresql://localhost:5433/detran";
   Properties parameters = new Properties();
   parameters.put("user", "khelmo");
   parameters.put("password", "khelmo");
   conn = DriverManager.getConnection(dbURL, parameters);

   long t3 = System.currentTimeMillis();
   tDelta = (t3 - t2) / 1000.0;
   System.out.println("connect db: " + String.valueOf(tDelta));


   ///QUERY//////////////////		
   Statement st = conn.createStatement();
   ResultSet rs = st.executeQuery("SELECT RENAVAM,TOT_MULTA,EMAIL FROM USUARIO");

   long t4 = System.currentTimeMillis();
   tDelta = (t4 - t3) / 1000.0;
   System.out.println("consulta db: " + String.valueOf(tDelta));

   while (rs.next()) {

    tentativas = 0;
    erro_captcha = 1;
    while ((erro_captcha == 1) && (tentativas < 3)) {
     t4 = System.currentTimeMillis();

     String renavam = rs.getString(1);
     String tot_multa = rs.getString(2);
     String email = rs.getString(3);
     /*
     System.out.println("Col 1: ");
     System.out.println(rs.getString(1));
			
     System.out.println("Col 2: ");
     System.out.println(rs.getString(2));
			
     System.out.println("Col 3: ");
     System.out.println(rs.getString(3)); */


     try {
      //logger.fine("Opening the page...");
      //System.out.println("Opening the page...");

      //driver.navigate().to("http://testing-ground.scraping.pro/captcha");

      driver.navigate().to("http://www2.detran.rj.gov.br/portal/multas/nadaConsta");

      long t5 = System.currentTimeMillis();
      tDelta = (t5 - t4) / 1000.0;
      System.out.println("navegar detran: " + String.valueOf(tDelta));

      byte[] arrScreen = driver.getScreenshotAs(OutputType.BYTES);
      BufferedImage imageScreen = ImageIO.read(new ByteArrayInputStream(arrScreen));

      //WebElement cap = driver.findElementById("captcha");
      WebElement cap = driver.findElementById("imgCaptcha");

      Dimension capDimension = cap.getSize();
      Point capLocation = cap.getLocation();
      BufferedImage imgCap = imageScreen.getSubimage(capLocation.x, capLocation.y, capDimension.width, capDimension.height - 18);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(imgCap, "png", os);

      SocketClient client = new SocketClient("khelmo", "fluzao4torrada");
      //logger.fine("Sending request to DeathByCaptcha...");

      //File captchaFileName = new File("C:/desenv/detran/testes/captcha_image.jpg");
      //FileInputStream fis = new FileInputStream(captchaFileName);

      //System.out.println("Sending request to DeathByCaptcha...");
      /* Put your CAPTCHA file name, or file object, or arbitrary input stream,
		           or an array of bytes, and optional solving timeout (in seconds) here: */

      long t6 = System.currentTimeMillis();
      tDelta = (t6 - t5) / 1000.0;
      System.out.println("preparar captcha: " + String.valueOf(tDelta));


      Captcha res = client.decode(new ByteArrayInputStream(os.toByteArray()), 20);
      //Captcha res = client.decode(fis, 20);

      long t7 = System.currentTimeMillis();
      tDelta = (t7 - t6) / 1000.0;
      System.out.println("decode captcha: " + String.valueOf(tDelta));

      //System.out.println("Checking response from DeathByCaptcha...");
      if (res != null && res.isSolved() && res.isCorrect()) {
       erro_captcha = 0;
       //logger.info(res.text);
       //System.out.println(res.text);
       /*
        * MultasRenavam
        * MultasCode
        * 
        * btPesquisar
        */

       //driver.findElementByXPath("//input[@name='MultasRenavam']").sendKeys("00152265503");
       //driver.findElementByXPath("//input[@name='MultasCode']").sendKeys(res.text);
       //driver.findElementByXPath("//input[@name='submit']").click();

       driver.findElementById("MultasRenavam").sendKeys(renavam);
       //driver.findElementById("MultasRenavam").sendKeys("00152419829");
                                                           
       driver.findElementById("MultasCode").sendKeys(res.text);
       driver.findElementById("btPesquisar").click();

       long t8 = System.currentTimeMillis();
       tDelta = (t8 - t7) / 1000.0;
       System.out.println("click pesquisar: " + String.valueOf(tDelta));

       int achou = 1;
       try {
        driver.findElementById("erroCaptchaTop");
       } catch (NoSuchElementException e) {
        achou = 0;
       }

       //achou erro na captcha
       if (achou == 1) {
        //report todo
        erro_captcha = 1;
       } else {
        //System.out.println(driver.findElementById("caixaInformacao").getText());
        String caixa = driver.findElementById("caixaInformacao").getText();
        int n_multa = -1;

        if (caixa.toLowerCase().contains("as inform")) {
         String multa = driver.findElementByClassName("paginate").getText();
         multa = multa.substring(0, multa.indexOf(" ")).trim();
         n_multa = Integer.parseInt(multa);

        } else {
         n_multa = 0;
        }
        //System.out.println(n_multa);

        long t9 = System.currentTimeMillis();
        tDelta = (t9 - t8) / 1000.0;
        System.out.println("parse multa: " + String.valueOf(tDelta));


        PreparedStatement pst = conn.prepareStatement("UPDATE USUARIO SET TOT_MULTA = ? WHERE RENAVAM = ?");
        //-1 novo usu, só atualiza
        if (Integer.parseInt(tot_multa) == -1) {
         ///update/////////////								
         pst.setInt(1, n_multa);
         pst.setString(2, renavam);
         int rowsUpdated = pst.executeUpdate();
         //	System.out.println("Total atualizado: "+rowsUpdated);

         ///update/////////////	
        } else if (n_multa > Integer.parseInt(tot_multa)) {
         ///numero maior, recebeu multa								
         pst.setInt(1, n_multa);
         pst.setString(2, renavam);
         int rowsUpdated = pst.executeUpdate();
         //	System.out.println("Total atualizado: "+rowsUpdated);

         ///update/////////////

         //TODO envia notificacao
         //consultamultadetran@gmail.com
         //pwdconsulta
         
         long t = System.currentTimeMillis();
         SendEmail mailSender;
         mailSender = new SendEmail(email, "Nova multa no Detran-RJ", "Caro usuário, você tem uma nova multa constante no site do Detran-RJ. Verifique acessando o site do Detran.");
         
         long t0 = System.currentTimeMillis();
         tDelta = (t0 - t) / 1000.0;
         System.out.println("enviar email: " + String.valueOf(tDelta));

        } else if (n_multa < Integer.parseInt(tot_multa)) {
            ///numero menor algo sumiu do nada consta						
            pst.setInt(1, n_multa);
            pst.setString(2, renavam);
            int rowsUpdated = pst.executeUpdate();
            //	System.out.println("Total atualizado: "+rowsUpdated);

            ///update/////////////

            //TODO envia notificacao
            //consultamultadetran@gmail.com
            //pwdconsulta
            
            long t = System.currentTimeMillis();
            SendEmail mailSender;
            mailSender = new SendEmail(email, "Nada Consta no Detran-RJ alterado", "Caro usuário, o nro de multas do Nada Consta de seu veículo no site do Detran-RJ foi alterado. Verifique acessando o site do Detran.");
            
            long t0 = System.currentTimeMillis();
            tDelta = (t0 - t) / 1000.0;
            System.out.println("enviar email: " + String.valueOf(tDelta));

           }
        pst.close();

       }


      } else {
       //System.out.println(res.toString());
       logger.severe("Captcha recognition error occured");
       tentativas = tentativas + 1;
      }
     } catch (Exception | IOException | InterruptedException e) {

      logger.severe(String.format("IO exception: %s", e.getMessage()));
     } finally {
      //	driver.close();
     }



    }
    ///QUERY//////////////////




   }
   rs.close();
   st.close();
   conn.close();
  } catch (SQLException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
   System.out.println("erro sql exception");
   erro_captcha = 1;
  }
 }

 /**
  * The logger.
  */
 private static Logger logger;
}