# Consulta-Detran
This application acess the website of Rio de Janeiro Department of Transit, then it searches for tickets of a given car registration. If there are any new tickets it sends an email to a user.



This is a personal project I did for a friend who owns a taxi and rented it to other drivers. In case this other driver got a speeding ticket it would be in my friends license, and then he would have to ask the department to replace it for the driver who was actually speeding. The problem is there's a deadline for that request. By automatically checking the site for new tickets he wouldn't miss this deadline.

I used selenium webdriver for automatic navigation of the site. The search form has a captcha and I used DeathByCaptcha Services to decode it. The license and email are stored in a postgres database.
